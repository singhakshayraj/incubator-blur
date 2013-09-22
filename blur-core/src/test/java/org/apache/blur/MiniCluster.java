package org.apache.blur;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.apache.blur.utils.BlurConstants.BLUR_GUI_CONTROLLER_PORT;
import static org.apache.blur.utils.BlurConstants.BLUR_GUI_SHARD_PORT;
import static org.apache.blur.utils.BlurConstants.BLUR_SHARD_BLOCKCACHE_DIRECT_MEMORY_ALLOCATION;
import static org.apache.blur.utils.BlurConstants.BLUR_SHARD_BLOCKCACHE_SLAB_COUNT;
import static org.apache.blur.utils.BlurConstants.BLUR_SHARD_HOSTNAME;
import static org.apache.blur.utils.BlurConstants.BLUR_SHARD_SAFEMODEDELAY;
import static org.apache.blur.utils.BlurConstants.BLUR_ZOOKEEPER_CONNECTION;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.blur.log.Log;
import org.apache.blur.log.LogFactory;
import org.apache.blur.manager.clusterstatus.ZookeeperPathConstants;
import org.apache.blur.store.buffer.BufferStore;
import org.apache.blur.thirdparty.thrift_0_9_0.TException;
import org.apache.blur.thirdparty.thrift_0_9_0.transport.TTransportException;
import org.apache.blur.thrift.BlurClient;
import org.apache.blur.thrift.ThriftBlurControllerServer;
import org.apache.blur.thrift.ThriftBlurShardServer;
import org.apache.blur.thrift.ThriftServer;
import org.apache.blur.thrift.generated.Blur.Iface;
import org.apache.blur.thrift.generated.BlurException;
import org.apache.blur.thrift.generated.BlurQuery;
import org.apache.blur.thrift.generated.BlurResults;
import org.apache.blur.thrift.generated.Column;
import org.apache.blur.thrift.generated.Record;
import org.apache.blur.thrift.generated.Row;
import org.apache.blur.thrift.generated.RowMutation;
import org.apache.blur.thrift.generated.TableDescriptor;
import org.apache.blur.thrift.util.BlurThriftHelper;
import org.apache.blur.utils.BlurUtil;
import org.apache.blur.zookeeper.ZooKeeperClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

public class MiniCluster {

  private static Log LOG = LogFactory.getLog(MiniCluster.class);
  private MiniDFSCluster cluster;
  private Thread serverThread;
  // private String zkConnectionString = "localhost:21810";
  private ZooKeeperServerMainEmbedded zooKeeperServerMain;
  private List<ThriftServer> controllers = new ArrayList<ThriftServer>();
  private List<ThriftServer> shards = new ArrayList<ThriftServer>();

  public static void main(String[] args) throws IOException, InterruptedException, KeeperException, BlurException,
      TException {
    MiniCluster miniCluster = new MiniCluster();
    miniCluster.startDfs("./tmp/hdfs");
    miniCluster.startZooKeeper("./tmp/zk");
    miniCluster.startControllers(1, false);
    miniCluster.startShards(1, false);

    try {
      Iface client = BlurClient.getClient(miniCluster.getControllerConnectionStr());
      miniCluster.createTable("test", client);
      long start = System.nanoTime();
      for (int i = 0; i < 1000; i++) {
        long now = System.nanoTime();
        if (start + 5000000000L < now) {
          System.out.println("Total [" + i + "]");
          start = now;
        }
        miniCluster.addRow("test", i, client);
      }

      // This waits for all the data to become visible.
      Thread.sleep(2000);

      for (int i = 0; i < 1000; i++) {
        miniCluster.searchRow("test", i, client);
      }

    } finally {
      miniCluster.stopShards();
      miniCluster.stopControllers();
      miniCluster.shutdownZooKeeper();
      miniCluster.shutdownDfs();
    }
  }

  public void startBlurCluster(String path, int controllerCount, int shardCount) {
    startBlurCluster(path, controllerCount, shardCount, false);
  }

  public void startBlurCluster(String path, int controllerCount, int shardCount, boolean randomPort) {
    startDfs(path + "/hdfs");
    startZooKeeper(path + "/zk", randomPort);
    setupBuffers();
    startControllers(controllerCount, randomPort);
    startShards(shardCount, randomPort);
  }

  private void setupBuffers() {
    BufferStore.init(16, 16);
  }

  public void shutdownBlurCluster() {
    stopShards();
    stopControllers();
    shutdownZooKeeper();
    shutdownDfs();
  }

  private void createTable(String test, Iface client) throws BlurException, TException, IOException {
    final TableDescriptor descriptor = new TableDescriptor();
    descriptor.setName(test);
    descriptor.setShardCount(7);
    descriptor.setTableUri(getFileSystemUri() + "/blur/" + test);
    client.createTable(descriptor);
  }

  public String getControllerConnectionStr() {
    StringBuilder builder = new StringBuilder();
    for (ThriftServer server : controllers) {
      if (builder.length() != 0) {
        builder.append(',');
      }
      String hostName = server.getServerTransport().getBindAddr().getHostName();
      int localPort = server.getServerTransport().getServerSocket().getLocalPort();
      builder.append(hostName + ":" + localPort);
    }
    return builder.toString();
  }

  private void addRow(String table, int i, Iface client) throws BlurException, TException {
    Row row = new Row();
    row.setId(Integer.toString(i));
    Record record = new Record();
    record.setRecordId(Integer.toString(i));
    record.setFamily("test");
    record.addToColumns(new Column("test", Integer.toString(i)));
    row.addToRecords(record);
    RowMutation rowMutation = BlurUtil.toRowMutation(table, row);
    rowMutation.setWal(false);
    client.mutate(rowMutation);
  }

  private void searchRow(String table, int i, Iface client) throws BlurException, TException {
    BlurQuery blurQuery = BlurThriftHelper.newSimpleQuery("test.test:" + i);
    System.out.println("Running [" + blurQuery + "]");
    BlurResults results = client.query(table, blurQuery);
    if (results.getTotalResults() != 1L) {
      throw new RuntimeException("we got a problem here.");
    }
  }

  public void stopControllers() {
    for (ThriftServer s : controllers) {
      s.close();
    }
  }

  public void stopShards() {
    for (ThriftServer s : shards) {
      s.close();
    }
  }

  public void startControllers(int num, boolean randomPort) {
    BlurConfiguration configuration = getBlurConfiguration();
    startControllers(configuration, num, randomPort);
  }

  private BlurConfiguration getBlurConfiguration(BlurConfiguration overrides) {
    BlurConfiguration conf = getBlurConfiguration();

    for (Map.Entry<String, String> over : overrides.getProperties().entrySet()) {
      conf.set(over.getKey().toString(), over.getValue().toString());
    }
    return conf;
  }

  private BlurConfiguration getBlurConfiguration() {
    BlurConfiguration configuration;
    try {
      configuration = new BlurConfiguration();
    } catch (IOException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    }
    configuration.set(BLUR_ZOOKEEPER_CONNECTION, getZkConnectionString());
    configuration.set(BLUR_SHARD_BLOCKCACHE_DIRECT_MEMORY_ALLOCATION, "false");
    configuration.set(BLUR_SHARD_BLOCKCACHE_SLAB_COUNT, "0");
    configuration.setLong(BLUR_SHARD_SAFEMODEDELAY, 5000);
    configuration.setInt(BLUR_GUI_CONTROLLER_PORT, -1);
    configuration.setInt(BLUR_GUI_SHARD_PORT, -1);

    return configuration;
  }

  public void startControllers(BlurConfiguration configuration, int num, boolean randomPort) {
    BlurConfiguration localConf = getBlurConfiguration(configuration);
    for (int i = 0; i < num; i++) {
      try {
        ThriftServer server = ThriftBlurControllerServer.createServer(i, localConf, randomPort);
        controllers.add(server);
        startServer(server);
      } catch (Exception e) {
        LOG.error(e);
        throw new RuntimeException(e);
      }
    }
  }

  public void startShards(int num, boolean randomPort) {
    BlurConfiguration configuration = getBlurConfiguration();
    startShards(configuration, num, randomPort);
  }

  public void startShards(final BlurConfiguration configuration, int num, final boolean randomPort) {
    final BlurConfiguration localConf = getBlurConfiguration(configuration);
    ExecutorService executorService = Executors.newFixedThreadPool(num);
    List<Future<ThriftServer>> futures = new ArrayList<Future<ThriftServer>>();
    for (int i = 0; i < num; i++) {
      final int index = i;
      futures.add(executorService.submit(new Callable<ThriftServer>() {
        @Override
        public ThriftServer call() throws Exception {
          return ThriftBlurShardServer.createServer(index, localConf, randomPort);
        }
      }));
    }
    for (int i = 0; i < num; i++) {
      try {
        ThriftServer server = futures.get(i).get();
        shards.add(server);
        startServer(server);
      } catch (Exception e) {
        LOG.error(e);
        throw new RuntimeException(e);
      }
    }
  }

  public void killShardServer(int shardServer) throws IOException, InterruptedException, KeeperException {
    killShardServer(getBlurConfiguration(), shardServer);
  }

  public void killShardServer(final BlurConfiguration configuration, int shardServer) throws IOException,
      InterruptedException, KeeperException {
    ThriftServer thriftServer = shards.get(shardServer);
    int shardPort = thriftServer.getServerTransport().getServerSocket().getLocalPort();
    String nodeNameHostname = ThriftServer.getNodeName(configuration, BLUR_SHARD_HOSTNAME);
    String nodeName = nodeNameHostname + ":" + shardPort;
    ZooKeeper zk = new ZooKeeperClient(getZkConnectionString(), 30000, new Watcher() {
      @Override
      public void process(WatchedEvent event) {

      }
    });
    String onlineShardsPath = ZookeeperPathConstants
        .getOnlineShardsPath(org.apache.blur.utils.BlurConstants.BLUR_CLUSTER);
    String path = onlineShardsPath + "/" + nodeName;
    zk.delete(path, -1);
    zk.close();
  }

  private static void startServer(final ThriftServer server) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          server.start();
        } catch (TTransportException e) {
          LOG.error(e);
          throw new RuntimeException(e);
        }
      }
    }).start();
    while (true) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        return;
      }
      int localPort = server.getLocalPort();
      if (localPort == 0) {
        continue;
      } else {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          LOG.error("Unknown error", e);
        }
        return;
      }
    }
  }

  public String getZkConnectionString() {
    return zooKeeperServerMain.getConnectionString();
  }

  public void startZooKeeper(String path) {
    startZooKeeper(true, path, false);
  }

  public void startZooKeeper(String path, boolean randomPort) {
    startZooKeeper(true, path, randomPort);
  }

  public void startZooKeeper(boolean format, String path) {
    startZooKeeper(format, path, false);
  }

  public void startZooKeeper(boolean format, String path, boolean randomPort) {
    Properties properties = new Properties();
    properties.setProperty("tickTime", "2000");
    properties.setProperty("initLimit", "10");
    properties.setProperty("syncLimit", "5");

    properties.setProperty("clientPort", "21810");

    startZooKeeper(properties, format, path, randomPort);
  }

  public void startZooKeeper(Properties properties, String path) {
    startZooKeeper(properties, true, path, false);
  }

  public void startZooKeeper(Properties properties, String path, boolean randomPort) {
    startZooKeeper(properties, true, path, randomPort);
  }

  private class ZooKeeperServerMainEmbedded extends ZooKeeperServerMain {
    @Override
    public void shutdown() {
      super.shutdown();
    }

    public String getConnectionString() {
      try {
        Field field = ZooKeeperServerMain.class.getDeclaredField("cnxnFactory");
        field.setAccessible(true);
        ServerCnxnFactory serverCnxnFactory = (ServerCnxnFactory) field.get(this);
        InetSocketAddress address = serverCnxnFactory.getLocalAddress();
        if (address == null) {
          return null;
        }
        int localPort = serverCnxnFactory.getLocalPort();
        return address.getAddress().getHostAddress() + ":" + localPort;
      } catch (NullPointerException e) {
        return null;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void startZooKeeper(final Properties properties, boolean format, String path, final boolean randomPort) {
    String realPath = path + "/zk_test";
    properties.setProperty("dataDir", realPath);
    final ServerConfig serverConfig = new ServerConfig();
    QuorumPeerConfig config = new QuorumPeerConfig() {
      @Override
      public InetSocketAddress getClientPortAddress() {
        InetSocketAddress clientPortAddress = super.getClientPortAddress();
        if (randomPort) {
          return randomPort(clientPortAddress);
        }
        return clientPortAddress;
      }

      private InetSocketAddress randomPort(InetSocketAddress clientPortAddress) {
        return new InetSocketAddress(clientPortAddress.getAddress(), 0);
      }
    };
    try {
      config.parseProperties(properties);
    } catch (IOException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    } catch (ConfigException e) {
      LOG.error(e);
      throw new RuntimeException(e);
    }
    serverConfig.readFrom(config);
    rm(new File(realPath));
    serverThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          zooKeeperServerMain = new ZooKeeperServerMainEmbedded();
          zooKeeperServerMain.runFromConfig(serverConfig);
        } catch (IOException e) {
          LOG.error(e);
        }
      }
    });
    serverThread.start();
    long s = System.nanoTime();
    while (s + 10000000000L > System.nanoTime()) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        LOG.error(e);
        throw new RuntimeException(e);
      }
      try {
        String zkConnectionString = getZkConnectionString();
        if (zkConnectionString == null) {
          continue;
        }
        ZooKeeper zk = new ZooKeeper(getZkConnectionString(), 30000, new Watcher() {
          @Override
          public void process(WatchedEvent event) {

          }
        });
        zk.close();
        break;
      } catch (IOException e) {
        LOG.error(e);
        throw new RuntimeException(e);
      } catch (InterruptedException e) {
        LOG.error(e);
        throw new RuntimeException(e);
      }
    }
  }

  public URI getFileSystemUri() throws IOException {
    return cluster.getFileSystem().getUri();
  }

  public void startDfs(String path) {
    startDfs(true, path);
  }

  public void startDfs(boolean format, String path) {
    startDfs(new Configuration(), format, path);
  }

  public void startDfs(Configuration conf, String path) {
    startDfs(conf, true, path);
  }

  public void startDfs(Configuration conf, boolean format, String path) {
    Properties properties = System.getProperties();

//    Set<Object> keySet = properties.keySet();
//    for (Object k : keySet) {
//      String key = k.toString();
//      if (key.contains("os")) {
//        String value = properties.getProperty(key);
//        System.out.println("Key [" + key + "] Value [" + value + "]");
//      }
//    }

    String osName = properties.getProperty("os.name").toLowerCase();
//    System.out.println("os.name=" + osName);
    if (osName.contains("linux")) {
      conf.set("dfs.datanode.data.dir.perm", "775");
    }
    System.setProperty("test.build.data", path);
    try {
      cluster = new MiniDFSCluster(conf, 1, true, (String[]) null);
      cluster.waitActive();
    } catch (Exception e) {
      LOG.error("error opening file system", e);
      throw new RuntimeException(e);
    }
  }

  public void shutdownZooKeeper() {
    zooKeeperServerMain.shutdown();
  }

  public void shutdownDfs() {
    if (cluster != null) {
      LOG.info("Shutting down Mini DFS ");
      try {
        cluster.shutdown();
      } catch (Exception e) {
        // / Can get a java.lang.reflect.UndeclaredThrowableException thrown
        // here because of an InterruptedException. Don't let exceptions in
        // here be cause of test failure.
      }
      try {
        FileSystem fs = cluster.getFileSystem();
        if (fs != null) {
          LOG.info("Shutting down FileSystem");
          fs.close();
        }
        FileSystem.closeAll();
      } catch (IOException e) {
        LOG.error("error closing file system", e);
      }

      // This has got to be one of the worst hacks I have ever had to do.
      // This is needed to shutdown 2 thread pools that are not shutdown by
      // themselves.
      ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
      Thread[] threads = new Thread[100];
      int enumerate = threadGroup.enumerate(threads);
      for (int i = 0; i < enumerate; i++) {
        Thread thread = threads[i];
        if (thread.getName().startsWith("pool")) {
          if (thread.isAlive()) {
            thread.interrupt();
            LOG.info("Stopping ThreadPoolExecutor [" + thread.getName() + "]");
            Object target = getField(thread, "target");
            if (target != null) {
              ThreadPoolExecutor e = (ThreadPoolExecutor) getField(target, "this$0");
              if (e != null) {
                e.shutdownNow();
              }
            }
            try {
              LOG.info("Waiting for thread pool to exit [" + thread.getName() + "]");
              thread.join();
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        }
      }
    }
  }

  private static Object getField(Object o, String fieldName) {
    try {
      Field field = o.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(o);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void rm(File file) {
    if (!file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        rm(f);
      }
    }
    file.delete();
  }

}
