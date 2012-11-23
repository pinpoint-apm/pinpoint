package com.profiler.common.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.hbase.HBaseQuery.HbaseColumn;

@Deprecated
public class HBaseClient {

    private static final Logger LOG = LoggerFactory.getLogger(HBaseClient.class);

    private Configuration configuration;
    private HTablePool tablePool;

    private final Map<String, HTableInterface> htableList = new HashMap<String, HTableInterface>();

    public HBaseClient(Properties properties) {
        String host = properties.getProperty("hbase.client.host", "localhost");
        String port = properties.getProperty("hbase.client.port", "2181");
        this.configuration = createConfiguration(host, port);
        Integer poolSize = NumberUtils.toInt(properties.getProperty("hbase.client.poolSize"), 16);
        init(configuration, poolSize);
    }

    public HBaseClient(String zk, String port, int poolSize) {
        Configuration cfg = createConfiguration(zk, port);
        init(cfg, poolSize);
    }

    private Configuration createConfiguration(String zk, String port) {
        Configuration cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", zk);
        cfg.set("hbase.zookeeper.property.clientPort", port);
        return cfg;
    }

    public HBaseClient(Configuration configuration, int poolSize) {
        init(configuration, poolSize);
    }

    private void init(Configuration configuration, int poolSize) {
        tablePool = new HTablePool(configuration, poolSize);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void close() {
        // htableList는 안지워워도 되지.
        try {
            tablePool.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
    }

    HTablePool getTablePool() {
        return tablePool;
    }

    public Iterator<Map<String, byte[]>> getHBaseData(HBaseQuery query) {
        ResultSetIterator r = new ResultSetIterator(query);
        return r.getIterator();
    }

    private HTableInterface getHTable(String tableName) {
        HTableInterface htable = null;
        synchronized (htableList) {
            htable = htableList.get(tableName);
            if (htable == null) {
                htable = tablePool.getTable(tableName);
                htableList.put(tableName, htable);
            }
        }
        return htable;
    }


    public void insert(String tableName, Put put) {
        HTable htable = (HTable) tablePool.getTable(tableName);
        try {
            htable.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeHTable(htable);
        }
    }

    public void insert(String tableName, List<Put> put) {
        HTable htable = (HTable) tablePool.getTable(tableName);
        try {
            htable.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeHTable(htable);
        }
    }

    public void delete(String tableName, Delete delete) {
        HTable htable = (HTable) tablePool.getTable(tableName);
        try {
            htable.delete(delete);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeHTable(htable);
        }
    }

    public void execute(String tableName, HTableCallBack callBack) {
        HTable htable = (HTable) tablePool.getTable(tableName);
        try {
            callBack.doExecute(htable);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO ex 처리
        } finally {
            closeHTable(htable);
        }
    }

    public Result[] get(String tablename, List<Get> get) {
        HTable htable = (HTable) tablePool.getTable(tablename);

        try {
            return htable.get(get);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO ex 처리
        } finally {
            closeHTable(htable);
        }
        return new Result[0];
    }

    private void closeHTable(HTable htable) {
        if (htable != null) {
            try {
                htable.close();
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    private class ResultSetIterator {
        ResultScanner resultScanner = null;
        Iterator<Result> resultIterator;
        List<HbaseColumn> columns;
        Iterator<Map<String, byte[]>> rSetIterator;

        public ResultSetIterator(HBaseQuery query) {
            try {
                byte[] startRow = query.getStartRow();
                byte[] stopRow = query.getStopRow();
                String tableName = query.getTableName();

                columns = query.getColumns();
                HTableInterface htable = getHTable(tableName);

                if (query.isSingleRow()) {
                    Get get = new Get(startRow);

                    if (columns != null) {
                        for (HbaseColumn column : columns) {
                            get.addColumn(column.getFamily().getBytes(), column.getColumnName().getBytes());
                        }
                    }

                    Result result = htable.get(get);
                    List<Result> resultList = new ArrayList<Result>(1);
                    resultList.add(result);
                    resultIterator = resultList.iterator();
                } else {
                    Scan scan = new Scan();

                    if (startRow != null) {
                        scan.setStartRow(startRow);
                    }

                    if (stopRow != null) {
                        scan.setStopRow(stopRow);
                    }

                    if (columns != null) {
                        for (HbaseColumn column : columns) {
                            scan.addColumn(column.getFamily().getBytes(), column.getColumnName().getBytes());
                        }
                    }

                    LOG.debug("Executing scanner: " + query);

                    long start = System.currentTimeMillis();

                    resultScanner = htable.getScanner(scan);

                    LOG.trace("Time taken for scanner: " + (System.currentTimeMillis() - start));

                    resultIterator = resultScanner.iterator();
                }
            } catch (Exception e) {
                throw new RuntimeException("UNable to execute SCANNER : " + query, e);
            }

            if (!resultIterator.hasNext()) {
                rSetIterator = new ArrayList<Map<String, byte[]>>().iterator();
                return;
            }

            rSetIterator = new Iterator<Map<String, byte[]>>() {
                public boolean hasNext() {
                    return hasnext();
                }

                public Map<String, byte[]> next() {
                    return getARow();
                }

                public void remove() {
                }
            };
        }

        private Iterator<Map<String, byte[]>> getIterator() {
            return rSetIterator;
        }

        private Map<String, byte[]> getARow() {
            if (resultIterator == null)
                return null;
            Result res = resultIterator.next();

            Map<String, byte[]> result = new HashMap<String, byte[]>();
            if (!res.isEmpty()) {
                byte[] value;
                if (columns != null) {
                    for (HbaseColumn column : columns) {
                        String colName = column.getColumnName();
                        value = res.getValue(column.getFamily().getBytes(), column.getColumnName().getBytes());

                        if (value == null) {
                            continue;
                        }

                        result.put(colName, value);
                    }
                }
                value = res.getRow();
            }

            return result;
        }

        private boolean hasnext() {
            if (resultIterator == null)
                return false;
            try {
                if (resultIterator.hasNext()) {
                    return true;
                } else {
                    close();
                    return false;
                }
            } catch (Exception e) {
                close();
                e.printStackTrace();
                return false;
            }
        }

        private void close() {
            try {
                if (resultScanner != null) {
                    resultScanner.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }
}
