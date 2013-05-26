package org.apache.blur.manager.writer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.blur.log.Log;
import org.apache.blur.log.LogFactory;
import org.apache.blur.manager.BlurPartitioner;
import org.apache.blur.server.ShardContext;
import org.apache.blur.store.hdfs.HdfsDirectory;
import org.apache.blur.utils.BlurConstants;
import org.apache.blur.utils.BlurUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.CompositeReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.NRTManager.TrackingIndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

public class IndexImporter extends TimerTask implements Closeable {
  private final static Log LOG = LogFactory.getLog(IndexImporter.class);

  private final TrackingIndexWriter _trackingWriter;
  private final ReadWriteLock _lock;
  private final ShardContext _shardContext;
  private final Timer _timer;

  public IndexImporter(TrackingIndexWriter trackingWriter, ReadWriteLock lock, ShardContext shardContext,
      TimeUnit refreshUnit, long refreshAmount) {
    _trackingWriter = trackingWriter;
    _lock = lock;
    _shardContext = shardContext;
    _timer = new Timer("IndexImporter [" + shardContext.getShard() + "/" + shardContext.getTableContext().getTable()
        + "]", true);
    long period = refreshUnit.toMillis(refreshAmount);
    _timer.schedule(this, period, period);
  }

  @Override
  public void close() throws IOException {
    _timer.cancel();
    _timer.purge();
  }

  @Override
  public void run() {
    Path path = _shardContext.getHdfsDirPath();
    Configuration configuration = _shardContext.getTableContext().getConfiguration();
    try {
      FileSystem fileSystem = path.getFileSystem(configuration);
      SortedSet<FileStatus> listStatus = sort(fileSystem.listStatus(path));
      List<HdfsDirectory> indexesToImport = new ArrayList<HdfsDirectory>();
      for (FileStatus fileStatus : listStatus) {
        Path file = fileStatus.getPath();
        if (fileStatus.isDir() && file.getName().endsWith(".commit")) {
          HdfsDirectory hdfsDirectory = new HdfsDirectory(configuration, file);
          if (!DirectoryReader.indexExists(hdfsDirectory)) {
            LOG.error("Directory found at [{0}] is not a vaild index.", file);
          } else {
            indexesToImport.add(hdfsDirectory);
          }
        }
      }
      if (indexesToImport.isEmpty()) {
        return;
      }
      String table = _shardContext.getTableContext().getTable();
      String shard = _shardContext.getShard();
      for (Directory directory : indexesToImport) {
        LOG.info("About to import [{0}] into [{1}/{2}]", directory, shard, table);
      }
      LOG.info("Obtaining lock on [{0}/{1}]", shard, table);
      _lock.writeLock().lock();
      try {
        IndexWriter indexWriter = _trackingWriter.getIndexWriter();
        for (HdfsDirectory directory : indexesToImport) {
          LOG.info("Starting import [{0}], commiting on [{1}/{2}]", directory, shard, table);
          indexWriter.commit();
          boolean isSuccess = false;
          boolean isRollbackDueToException = false;
          try {
            isSuccess = applyDeletes(directory, indexWriter, shard);
          }catch(IOException e){
            LOG.error("Some issue with deleting the old index", e);
            isSuccess = false;
            isRollbackDueToException = true;
          }
          if (isSuccess) {
            LOG.info("Add index [{0}] [{1}/{2}]", directory, shard, table);
            indexWriter.addIndexes(directory);
            LOG.info("Finishing import [{0}], commiting on [{1}/{2}]", directory, shard, table);
            indexWriter.commit();
            Path dirPath = directory.getPath();
            LOG.info("Cleaning up old directory [{0}] for [{1}/{2}]", dirPath, shard, table);
            fileSystem.delete(dirPath, true);
            LOG.info("Import complete on [{0}/{1}]", shard, table);
          } else {
            if(!isRollbackDueToException){
              LOG.error("Index is corrupted, RowIds are found in wrong shard [{0}/{1}], cancelling index import for [{2}]", shard, table, directory);
            }
            LOG.info("Starting rollback on [{0}/{1}]", shard, table);
            indexWriter.rollback();
            LOG.info("Finished rollback on [{0}/{1}]", shard, table);
            Path oldDirPath = directory.getPath();
            String newDirectoryName = oldDirPath.getName().split("\\.")[0] + ".bad_rowids";
            fileSystem.rename(oldDirPath, new Path(oldDirPath.getParent(), newDirectoryName));
          }
        }
      } finally {
        _lock.writeLock().unlock();
      }
    } catch (IOException e) {
      LOG.error("Unknown error while trying to refresh imports.", e);
    }

  }

  private SortedSet<FileStatus> sort(FileStatus[] listStatus) {
    SortedSet<FileStatus> result = new TreeSet<FileStatus>();
    for (FileStatus fileStatus : listStatus) {
      result.add(fileStatus);
    }
    return result;
  }

  private boolean applyDeletes(Directory directory, IndexWriter indexWriter, String shard) throws IOException {
    DirectoryReader reader = DirectoryReader.open(directory);
    try {
      LOG.info("Applying deletes in reader [{0}]", reader);
      CompositeReaderContext compositeReaderContext = reader.getContext();
      List<AtomicReaderContext> leaves = compositeReaderContext.leaves();
      BlurPartitioner blurPartitioner = new BlurPartitioner();
      Text key = new Text();
      int numberOfShards = _shardContext.getTableContext().getDescriptor().getShardCount();
      for (AtomicReaderContext context : leaves) {
        AtomicReader atomicReader = context.reader();
        Fields fields = atomicReader.fields();
        Terms terms = fields.terms(BlurConstants.ROW_ID);
        TermsEnum termsEnum = terms.iterator(null);
        BytesRef ref = null;
        while ((ref = termsEnum.next()) != null) {
          byte[] rowIdInBytes = ref.bytes;
          key.set(rowIdInBytes, 0, rowIdInBytes.length);
          int partition = blurPartitioner.getPartition(key, null, numberOfShards);
          int shardId = BlurUtil.getShardIndex(shard);
          if( shardId != partition){
            return false;
          }
          Term term = new Term(BlurConstants.ROW_ID, BytesRef.deepCopyOf(ref));
          indexWriter.deleteDocuments(term);
        }
      }
    } finally {
      reader.close();
    }
    return true;
  }
}
