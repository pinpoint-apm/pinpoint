package com.profiler.common.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class HBaseQuery {

	private String tableName;

	private String startRow;

	private String stopRow;

	private List<HbaseColumn> columns;

	public static class HbaseColumn {

		String family;

		String columnName;

		public HbaseColumn(String family, String columnName) {
			this.family = family;
			this.columnName = columnName;
		}

		public String getFamily() {
			return family;
		}

		public String getColumnName() {
			return columnName;
		}

		@Override
		public String toString() {
			return "{" + family + "|" + columnName + "}";
		}

	}

	public HBaseQuery() {
		super();
	}

	public HBaseQuery(String tableName, String startRow, String stopRow, List<HbaseColumn> columns) {
		super();
		this.tableName = tableName;
		this.startRow = startRow;
		this.stopRow = stopRow;
		this.columns = columns;
	}

	public void setColumns(String columns) {
		StringTokenizer st = new StringTokenizer(columns, ",");

		this.columns = new ArrayList<HbaseColumn>(st.countTokens());

		// columns
		while (st.hasMoreElements()) {
			String column = ((String) st.nextElement()).trim();
			int separatorIndex = column.indexOf('|');

			String family = "";
			String columnName = "";

			if (separatorIndex > -1) {
				family = column.substring(0, separatorIndex);
				columnName = column.substring(separatorIndex + 1);
			}

			HbaseColumn hbcolumn = new HbaseColumn(family, columnName);
			this.columns.add(hbcolumn);
		}
	}

	public boolean isSingleRow() {
		if (startRow == null) {
			return false;
		}
		return startRow.equals(stopRow);
	}

	// getter and setter

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getStartRow() {
		return startRow;
	}

	public void setStartRow(String startRow) {
		this.startRow = startRow;
	}

	public String getStopRow() {
		return stopRow;
	}

	public void setStopRow(String stopRow) {
		this.stopRow = stopRow;
	}

	public List<HbaseColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<HbaseColumn> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "[tableName=" + tableName + ", startRow=" + startRow + ", stopRow=" + stopRow + ", columns=" + columns + "]";
	}

}
