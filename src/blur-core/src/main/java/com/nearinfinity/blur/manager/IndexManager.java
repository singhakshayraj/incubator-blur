package com.nearinfinity.blur.manager;

import static com.nearinfinity.blur.utils.RowSuperDocumentUtil.createSuperDocument;
import static com.nearinfinity.blur.utils.RowSuperDocumentUtil.getRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.util.Version;

import com.nearinfinity.blur.lucene.index.SuperDocument;
import com.nearinfinity.blur.lucene.search.BlurSearcher;
import com.nearinfinity.blur.lucene.search.SuperParser;
import com.nearinfinity.blur.manager.hits.HitsIterable;
import com.nearinfinity.blur.manager.hits.HitsIterableSearcher;
import com.nearinfinity.blur.manager.hits.MergerHitsIterable;
import com.nearinfinity.blur.manager.status.SearchStatus;
import com.nearinfinity.blur.thrift.generated.BlurException;
import com.nearinfinity.blur.thrift.generated.Column;
import com.nearinfinity.blur.thrift.generated.Facet;
import com.nearinfinity.blur.thrift.generated.FacetQuery;
import com.nearinfinity.blur.thrift.generated.FacetResult;
import com.nearinfinity.blur.thrift.generated.FetchResult;
import com.nearinfinity.blur.thrift.generated.Row;
import com.nearinfinity.blur.thrift.generated.Schema;
import com.nearinfinity.blur.thrift.generated.ScoreType;
import com.nearinfinity.blur.thrift.generated.SearchQuery;
import com.nearinfinity.blur.thrift.generated.SearchQueryStatus;
import com.nearinfinity.blur.thrift.generated.Selector;
import com.nearinfinity.blur.utils.BlurConstants;
import com.nearinfinity.blur.utils.BlurExecutorCompletionService;
import com.nearinfinity.blur.utils.ForkJoin;
import com.nearinfinity.blur.utils.PrimeDocCache;
import com.nearinfinity.blur.utils.TermDocIterable;
import com.nearinfinity.blur.utils.ForkJoin.Merger;
import com.nearinfinity.blur.utils.ForkJoin.ParallelCall;

public class IndexManager implements BlurConstants {

    private static final Version LUCENE_VERSION = Version.LUCENE_30;
    private static final Log LOG = LogFactory.getLog(IndexManager.class);

    private IndexServer indexServer;
    private ExecutorService executor;
    private Collection<SearchStatus> currentSearchStatusCollection = Collections.synchronizedSet(new HashSet<SearchStatus>());
    private Timer searchStatusCleanupTimer;
    private long searchStatusCleanupTimerDelay = TimeUnit.MINUTES.toMillis(1);

    public IndexManager() {
        // do nothing
    }

    public IndexManager init() {
        executor = Executors.newCachedThreadPool();
        searchStatusCleanupTimer = new Timer("Search-Status-Cleanup",true);
        searchStatusCleanupTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cleanupFinishedSearchStatuses();
            }
        }, searchStatusCleanupTimerDelay, searchStatusCleanupTimerDelay);
        return this;
    }

    public static void replace(IndexWriter indexWriter, Row row) throws IOException {
        replace(indexWriter, createSuperDocument(row));
    }

    public static void replace(IndexWriter indexWriter, SuperDocument document) throws IOException {
        synchronized (indexWriter) {
            indexWriter.deleteDocuments(new Term(ID, document.getId()));
            if (!replaceInternal(indexWriter, document)) {
                indexWriter.deleteDocuments(new Term(ID, document.getId()));
                if (!replaceInternal(indexWriter, document)) {
                    throw new IOException("SuperDocument too large, try increasing ram buffer size.");
                }
            }
        }
    }

    public void close() throws InterruptedException {
        executor.shutdownNow();
        indexServer.close();
        searchStatusCleanupTimer.cancel();
        searchStatusCleanupTimer.purge();
    }

    public void replaceRow(String table, Row row) throws BlurException {
        throw new RuntimeException("not implemented");
    }

    public void removeRow(String table, String id) throws BlurException {
        throw new RuntimeException("not implemented");
    }

    public void fetchRow(String table, Selector selector, FetchResult fetchResult) throws BlurException {
        IndexReader reader;
        try {
            String shard = getShard(selector.getLocationId());
            Map<String, IndexReader> indexReaders = indexServer.getIndexReaders(table);
            if (indexReaders == null) {
                LOG.error("Table [" + table + "] not found");
                throw new BlurException("Table [" + table + "] not found");
            }
            reader = indexReaders.get(shard);
            if (reader == null) {
                if (reader == null) {
                    LOG.error("Shard [" + shard + "] not found in table [" + table + "]");
                    throw new BlurException("Shard [" + shard + "] not found in table [" + table + "]");
                }
            }
        } catch (BlurException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Unknown error while trying to get the correct index reader for selector [" + selector + "].", e);
            throw new BlurException(e.getMessage());
        }
        try {
            fetchRow(reader, table, selector, fetchResult);
        } catch (Exception e) {
            LOG.error("Unknown error while trying to fetch row.", e);
            throw new BlurException(e.getMessage());
        }
    }
    
    /**
     * Location id format is <shard>/luceneid.
     * @param locationId
     * @return
     */
    private String getShard(String locationId) {
        String[] split = locationId.split("\\/");
        if (split.length != 2) {
            throw new IllegalArgumentException("Location id invalid [" + locationId + "]");
        }
        return split[0];
    }

    public HitsIterable search(final String table, SearchQuery searchQuery) throws Exception {
        final SearchStatus status = new SearchStatus(searchStatusCleanupTimerDelay,table,searchQuery).attachThread();
        addStatus(status);
        try {
            Map<String, IndexReader> indexReaders;
            try {
                indexReaders = indexServer.getIndexReaders(table);
            } catch (IOException e) {
                LOG.error("Unknown error while trying to fetch index readers.", e);
                throw new BlurException(e.getMessage());
            }
            Filter preFilter = parseFilter(table, searchQuery.preSuperFilter, false, searchQuery.type);
            Filter postFilter = parseFilter(table, searchQuery.postSuperFilter, true, searchQuery.type);
            final Query userQuery = parseQuery(searchQuery.queryStr, searchQuery.superQueryOn, 
                    indexServer.getAnalyzer(table), postFilter, preFilter, searchQuery.type);
            return ForkJoin.execute(executor, indexReaders.entrySet(),
                new ParallelCall<Entry<String, IndexReader>, HitsIterable>() {
                    @Override
                    public HitsIterable call(Entry<String, IndexReader> entry) throws Exception {
                        status.attachThread();
                        try {
                            IndexReader reader = entry.getValue();
                            String shard = entry.getKey();
                            BlurSearcher searcher = new BlurSearcher(reader, 
                                    PrimeDocCache.getTableCache().getShardCache(table).
                                    getIndexReaderCache(shard));
                            searcher.setSimilarity(indexServer.getSimilarity(table));
                            return new HitsIterableSearcher((Query) userQuery.clone(), table, shard, searcher);
                        } finally {
                            status.deattachThread();
                        }
                    }
                }).merge(new MergerHitsIterable(searchQuery.minimumNumberOfHits, searchQuery.maxQueryTime));
        } finally {
            status.deattachThread();
            removeStatus(status);
        }
    }

    public void cancelSearch(long userUuid) {
        for (SearchStatus status : currentSearchStatusCollection) {
            if (status.getUserUuid() == userUuid) {
                status.cancelSearch();
            }
        }
    }

    public List<SearchQueryStatus> currentSearches(String table) {
        List<SearchQueryStatus> result = new ArrayList<SearchQueryStatus>();
        for (SearchStatus status : currentSearchStatusCollection) {
            if (status.getTable().equals(table)) {
                result.add(status.getSearchQueryStatus());
            }
        }
        return result;
    }

    private Filter parseFilter(String table, String filter, boolean superQueryOn, ScoreType scoreType)
            throws ParseException, BlurException {
        if (filter == null) {
            return null;
        }
        return new QueryWrapperFilter(new SuperParser(LUCENE_VERSION, indexServer.getAnalyzer(table), superQueryOn,
                null, scoreType).parse(filter));
    }

    private Query parseQuery(String query, boolean superQueryOn, Analyzer analyzer, Filter postFilter,
            Filter preFilter, ScoreType scoreType) throws ParseException {
        Query result = new SuperParser(LUCENE_VERSION, analyzer, superQueryOn, preFilter, scoreType).parse(query);
        if (postFilter == null) {
            return result;
        }
        return new FilteredQuery(result, postFilter);
    }

    private void fetchRow(IndexReader reader, String table, Selector selector, FetchResult fetchResult)
            throws CorruptIndexException, IOException {
        fetchResult.table = table;
        String locationId = selector.locationId;
        int lastSlash = locationId.lastIndexOf('/');
        int docId = Integer.parseInt(locationId.substring(lastSlash + 1));
        if (selector.isRecordOnly()) {
            // select only the row for the given data or location id.
            if (reader.isDeleted(docId)) {
                fetchResult.exists = false;
                fetchResult.deleted = true;
                return;
            } else {
                fetchResult.exists = true;
                fetchResult.deleted = false;
                Document document = reader.document(docId, getFieldSelector(selector));
                fetchResult.record = getColumns(document);
                return;
            }
        } else {
            if (reader.isDeleted(docId)) {
                fetchResult.exists = false;
                fetchResult.deleted = true;
                return;
            } else {
                fetchResult.exists = true;
                fetchResult.deleted = false;
                String rowId = getRowId(reader, docId);
                TermDocs termDocs = reader.termDocs(new Term(ID, rowId));
                fetchResult.row = getRow(new TermDocIterable(termDocs, reader));
                return;
            }
        }
    }

    private String getRowId(IndexReader reader, int docId) throws CorruptIndexException, IOException {
        Document document = reader.document(docId, new FieldSelector() {
            private static final long serialVersionUID = 4912420100148752051L;

            @Override
            public FieldSelectorResult accept(String fieldName) {
                if (ID.equals(fieldName)) {
                    return FieldSelectorResult.LOAD_AND_BREAK;
                }
                return FieldSelectorResult.NO_LOAD;
            }
        });
        return document.get(ID);
    }

    private Set<Column> getColumns(Document document) {
        Map<String, Column> columns = new HashMap<String, Column>();
        List<Fieldable> fields = document.getFields();
        String columnFamily = null;
        for (Fieldable field : fields) {
            String name = field.name();
            if (columnFamily == null) {
                columnFamily = getColumnFamily(name);
            }
            String value = field.stringValue();
            Column column = columns.get(name);
            if (column == null) {
                column = new Column();
                column.setName(getColumnName(name));
                columns.put(name, column);
            }
            column.addToValues(value);
        }
        Set<Column> cols = new HashSet<Column>(columns.values());
        if (columnFamily != null) {
            Column column = new Column().setName("_columnFamily_");
            column.addToValues(columnFamily);
            cols.add(column);
        }
        return cols;
    }

    private String getColumnName(String fieldName) {
        return fieldName.substring(fieldName.lastIndexOf('.') + 1);
    }

    private String getColumnFamily(String fieldName) {
        return fieldName.substring(0, fieldName.lastIndexOf('.'));
    }

    private FieldSelector getFieldSelector(final Selector selector) {
        return new FieldSelector() {
            private static final long serialVersionUID = 4089164344758433000L;

            @Override
            public FieldSelectorResult accept(String fieldName) {
                if (ID.equals(fieldName)) {
                    return FieldSelectorResult.LOAD;
                }
                if (SUPER_KEY.equals(fieldName)) {
                    return FieldSelectorResult.LOAD;
                }
                if (PRIME_DOC.equals(fieldName)) {
                    return FieldSelectorResult.NO_LOAD;
                }
                if (selector.columnFamilies == null && selector.columns == null) {
                    return FieldSelectorResult.LOAD;
                }
                String columnFamily = getColumnFamily(fieldName);
                if (selector.columnFamilies != null && selector.columnFamilies.contains(columnFamily)) {
                    return FieldSelectorResult.LOAD;
                }
                String columnName = getColumnName(fieldName);
                if (selector.columns != null) {
                    Set<String> columns = selector.columns.get(columnFamily);
                    if (columns != null && columns.contains(columnName)) {
                        return FieldSelectorResult.LOAD;
                    }
                }
                return FieldSelectorResult.NO_LOAD;
            }
        };
    }

    private static boolean replaceInternal(IndexWriter indexWriter, SuperDocument document) throws IOException {
        long oldRamSize = indexWriter.ramSizeInBytes();
        for (Document doc : document.getAllDocumentsForIndexing()) {
            long newRamSize = indexWriter.ramSizeInBytes();
            if (newRamSize < oldRamSize) {
                LOG.info("Flush occur during writing of super document, start over.");
                return false;
            }
            oldRamSize = newRamSize;
            indexWriter.addDocument(doc);
        }
        return true;
    }

    public IndexServer getIndexServer() {
        return indexServer;
    }

    public IndexManager setIndexServer(IndexServer indexServer) {
        this.indexServer = indexServer;
        return this;
    }
    
    private void removeStatus(SearchStatus status) {
        status.setFinished(true);
    }

    private void addStatus(SearchStatus status) {
        currentSearchStatusCollection.add(status);
    }
    
    private void cleanupFinishedSearchStatuses() {
        Collection<SearchStatus> remove = new HashSet<SearchStatus>();
        for (SearchStatus status : currentSearchStatusCollection) {
            if (status.isValidForCleanUp()) {
                remove.add(status);
            }
        }
        currentSearchStatusCollection.removeAll(remove);
    }

    public long recordFrequency(String table, final String columnFamily, final String columnName, final String value) throws Exception {
        Map<String, IndexReader> indexReaders;
        try {
            indexReaders = indexServer.getIndexReaders(table);
        } catch (IOException e) {
            LOG.error("Unknown error while trying to fetch index readers.", e);
            throw new BlurException(e.getMessage());
        }
        return ForkJoin.execute(executor, indexReaders.entrySet(),
            new ParallelCall<Entry<String, IndexReader>, Long>() {
                @Override
                public Long call(Entry<String, IndexReader> input) throws Exception {
                    IndexReader reader = input.getValue();
                    return recordFrequency(reader,columnFamily,columnName,value);
                }
        }).merge(new Merger<Long>() {
            @Override
            public Long merge(BlurExecutorCompletionService<Long> service) throws Exception {
                long total = 0;
                while (service.getRemainingCount() > 0) {
                    total += service.take().get();
                }
                return total;
            }
        });
    }

    public List<String> terms(String table, final String columnFamily, final String columnName, final String startWith, final short size) throws Exception {
        Map<String, IndexReader> indexReaders;
        try {
            indexReaders = indexServer.getIndexReaders(table);
        } catch (IOException e) {
            LOG.error("Unknown error while trying to fetch index readers.", e);
            throw new BlurException(e.getMessage());
        }
        return ForkJoin.execute(executor, indexReaders.entrySet(),
            new ParallelCall<Entry<String, IndexReader>, List<String>>() {
                @Override
                public List<String> call(Entry<String, IndexReader> input) throws Exception {
                    IndexReader reader = input.getValue();
                    return terms(reader,columnFamily,columnName,startWith,size);
                }
        }).merge(new Merger<List<String>>() {
            @Override
            public List<String> merge(BlurExecutorCompletionService<List<String>> service) throws Exception {
                TreeSet<String> terms = new TreeSet<String>();
                while (service.getRemainingCount() > 0) {
                    terms.addAll(service.take().get());
                }
                return new ArrayList<String>(terms).subList(0, Math.min(size, terms.size()));
            }
        });
    }
    
    public static long recordFrequency(IndexReader reader, String columnFamily, String columnName, String value) throws IOException {
        return reader.docFreq(getTerm(columnFamily,columnName,value));
    }

    public static List<String> terms(IndexReader reader, String columnFamily, String columnName, String startWith, short size) throws IOException {
        Term term = getTerm(columnFamily, columnName, startWith);
        String field = term.field();
        List<String> terms = new ArrayList<String>(size);
        TermEnum termEnum = reader.terms(term);
        try {
            do {
                Term currentTerm = termEnum.term();
                if (currentTerm == null) {
                    return terms;
                }
                if (!currentTerm.field().equals(field)) {
                    break;
                }
                terms.add(currentTerm.text());
                if (terms.size() >= size) {
                    return terms;
                }
            } while (termEnum.next());
            return terms;
        } finally {
            termEnum.close();
        }
    }
    
    private static Term getTerm(String columnFamily, String columnName, String value) {
        return new Term(columnFamily + "." + columnName, value);
    }

    public Schema schema(String table) throws IOException {
        Schema schema = new Schema().setTable(table);
        schema.columnFamilies = new TreeMap<String, Set<String>>();
        Map<String, IndexReader> indexReaders = indexServer.getIndexReaders(table);
        for (IndexReader reader : indexReaders.values()) {
            Collection<String> fieldNames = reader.getFieldNames(FieldOption.ALL);
            for (String fieldName : fieldNames) {
                int index = fieldName.indexOf('.');
                if (index > 0) {
                    String columnFamily = fieldName.substring(0, index);
                    String column = fieldName.substring(index + 1);
                    Set<String> set = schema.columnFamilies.get(columnFamily);
                    if (set == null) {
                        set = new TreeSet<String>();
                        schema.columnFamilies.put(columnFamily, set);
                    }
                    set.add(column);
                }
            }
        }
        return schema;
    }

    public void facetSearch(String table, FacetQuery facetQuery, FacetResult facetResult) throws Exception {
        Map<String, IndexReader> indexReaders;
        try {
            indexReaders = indexServer.getIndexReaders(table);
        } catch (IOException e) {
            LOG.error("Unknown error while trying to fetch index readers.", e);
            throw new BlurException(e.getMessage());
        }
        SearchQuery searchQuery = facetQuery.searchQuery;
        if (!searchQuery.superQueryOn) {
            throw new BlurException("Only super queries are supported");
        }
        Filter preFilter = parseFilter(table, searchQuery.preSuperFilter, false, searchQuery.type);
        Filter postFilter = parseFilter(table, searchQuery.postSuperFilter, true, searchQuery.type);
        Query userQuery = parseQuery(searchQuery.queryStr, searchQuery.superQueryOn, 
                indexServer.getAnalyzer(table), postFilter, preFilter, searchQuery.type);
        for (Facet facet : facetQuery.facets) {
            long count = runFacet(table, indexReaders, userQuery, facet, Long.MAX_VALUE, facetQuery.maxQueryTime, searchQuery);
            facetResult.putToCounts(facet, count);
        }
    }

    private long runFacet(final String table, Map<String, IndexReader> indexReaders, Query userQuery, Facet facet, long minimumNumberOfHits, long maxQueryTime, SearchQuery searchQuery) throws Exception {
        final SearchStatus status = new SearchStatus(searchStatusCleanupTimerDelay,table,searchQuery,facet).attachThread();
        addStatus(status);
        try {
            Filter facetFilter = parseFilter(table, facet.queryStr, true, ScoreType.CONSTANT);
            final FilteredQuery facetQuery = new FilteredQuery(userQuery, facetFilter);
            HitsIterable hitsIterable = ForkJoin.execute(executor, indexReaders.entrySet(),
                new ParallelCall<Entry<String, IndexReader>, HitsIterable>() {
                    @Override
                    public HitsIterable call(Entry<String, IndexReader> entry) throws Exception {
                        status.attachThread();
                        try {
                            IndexReader reader = entry.getValue();
                            String shard = entry.getKey();
                            BlurSearcher searcher = new BlurSearcher(reader, 
                                    PrimeDocCache.getTableCache().getShardCache(table).
                                    getIndexReaderCache(shard));
                            searcher.setSimilarity(indexServer.getSimilarity(table));
                            return new HitsIterableSearcher((Query) facetQuery, table, shard, searcher);
                        } finally {
                            status.deattachThread();
                        }
                    }
                }).merge(new MergerHitsIterable(minimumNumberOfHits, maxQueryTime));
            return hitsIterable.getTotalHits();
        } finally {
            status.deattachThread();
            removeStatus(status);
        }
    }

    public long getSearchStatusCleanupTimerDelay() {
        return searchStatusCleanupTimerDelay;
    }

    public IndexManager setSearchStatusCleanupTimerDelay(long searchStatusCleanupTimerDelay) {
        this.searchStatusCleanupTimerDelay = searchStatusCleanupTimerDelay;
        return this;
    }
}
