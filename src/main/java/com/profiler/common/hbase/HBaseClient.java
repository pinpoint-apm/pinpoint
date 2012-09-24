package com.profiler.common.hbase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.hbase.HBaseQuery.HbaseColumn;

public class HBaseClient {

	private static final Logger LOG = LoggerFactory.getLogger(HBaseClient.class);

	public final static String HBASE_ROW_ID = "ROW_KEY";

	private HTablePool tablePool;
	private HBaseAdmin admin;

	private final Map<String, HTableInterface> htableList = new HashMap<String, HTableInterface>();
	private final Map<String, Integer> fieldNameVsType = new HashMap<String, Integer>();
	private boolean convertType = false;

    public HBaseClient(Properties properties) {
        String host = properties.getProperty("hbase.client.host", "localhost");
        String port = properties.getProperty("hbase.client.port", "2181");
        Integer poolSize = NumberUtils.toInt(properties.getProperty("hbase.client.poolSize"), 16);
        init(host, port, poolSize, null);
	}

	public HBaseClient(String zk, String port, int poolSize) {
		init(zk, port, poolSize, null);
	}

    public HBaseClient(String zk, String port, int poolSize, Configuration configuration) {
		init(zk, port, poolSize, configuration);
	}

    public HBaseClient(Configuration configuration, int poolSize) {
		init(null, null, poolSize, configuration);
	}

	private void init(String zk, String port, int poolSize, Configuration configuration) {
        if(configuration == null) {
		    Configuration cfg = HBaseConfiguration.create();
            if (zk != null) {
                cfg.set("hbase.zookeeper.quorum", zk);
            }
            if (port != null) {
                cfg.set("hbase.zookeeper.property.clientPort", port);
            }
            configuration = cfg;
        }

		tablePool = new HTablePool(configuration, poolSize);
		try {
			admin = new HBaseAdmin(configuration);
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		}
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

	public Iterator<Map<String, Object>> getHBaseData(HBaseQuery query) {
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

	public boolean isTableExists(final String tableName) {
		try {
			return admin.tableExists(tableName);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void dropTable(final String tableName) {
		try {
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createTable(HTableDescriptor td) {
		try {
			admin.createTable(td);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		Iterator<Map<String, Object>> rSetIterator;

		public ResultSetIterator(HBaseQuery query) {
			try {
				byte[] startRow = query.getStartRow();
				byte[] stopRow = query.getStopRow();
				String tableName = query.getTableName();

				System.out.println("startRow=" + startRow);
				System.out.println("stopRow=" + stopRow);
				System.out.println("tableName=" + tableName);

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
							System.out.println("add column to scanner " + column);
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
				rSetIterator = new ArrayList<Map<String, Object>>().iterator();
				return;
			}

			rSetIterator = new Iterator<Map<String, Object>>() {
				public boolean hasNext() {
					return hasnext();
				}

				public Map<String, Object> next() {
					return getARow();
				}

				public void remove() {
				}
			};
		}

		private Iterator<Map<String, Object>> getIterator() {
			return rSetIterator;
		}

		private void addConvertedType(byte[] value, String colName, Map<String, Object> result) {
			Integer type = fieldNameVsType.get(colName);

			if (type == null) {
				type = HBaseTypes.STRING;
			}
			switch (type) {
			case HBaseTypes.INTEGER:
				// result.put(colName, Bytes.toInt(value));
				result.put(colName, Integer.valueOf(Bytes.toString(value)));
				break;
			case HBaseTypes.FLOAT:
				// result.put(colName, Bytes.toFloat(value));
				result.put(colName, Float.valueOf(Bytes.toString(value)));
				break;
			case HBaseTypes.LONG:
				// result.put(colName, Bytes.toLong(value));
				result.put(colName, Long.valueOf(Bytes.toString(value)));
				break;
			case HBaseTypes.DOUBLE:
				// result.put(colName, Bytes.toDouble(value));
				result.put(colName, Double.valueOf(Bytes.toString(value)));
				break;
			case HBaseTypes.DATE:
				result.put(colName, new Date(Bytes.toLong(value)));
				// result.put(colName, new
				// Date(Long.valueOf(Bytes.toString(value))));
				break;
			case HBaseTypes.BOOLEAN:
				// result.put(colName, Bytes.toBoolean(value));
				result.put(colName, Boolean.valueOf(Bytes.toString(value)));
				break;
			case HBaseTypes.BINARY:
				result.put(colName, value);
				break;
			case HBaseTypes.STRING:
				result.put(colName, Bytes.toString(value));
				break;
			default:
				result.put(colName, Bytes.toString(value));
				break;
			}
		}

		private Map<String, Object> getARow() {
			if (resultIterator == null)
				return null;
			Result res = resultIterator.next();

			System.out.println("next=" + res);

			Map<String, Object> result = new HashMap<String, Object>();
			if (!res.isEmpty()) {
				byte[] value;
				if (columns != null) {
					for (HbaseColumn column : columns) {
						String colName = column.getColumnName();
						value = res.getValue(column.getFamily().getBytes(), column.getColumnName().getBytes());

						if (value == null) {
							continue;
						}

						if (!convertType) {
							result.put(colName, Bytes.toString(value));
							continue;
						}

						// convert type
						addConvertedType(value, colName, result);
					}
				}
				value = res.getRow();

				addConvertedType(value, HBASE_ROW_ID, result);
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

	public static final String CONVERT_TYPE = "convertType";
}
