package com.nhn.pinpoint.testweb;

import java.io.InputStream;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class DBUnitSupport {

	enum DataType {
		EXCEL, FLATXML
	}

	@Autowired
	private DataSource dataSource;

	protected void cleanInsertXmlData(String fileSource) {
		insertData(fileSource, DataType.FLATXML, DatabaseOperation.CLEAN_INSERT);
	}

	protected void cleanInsertXlsData(String fileSource) {
		insertData(fileSource, DataType.EXCEL, DatabaseOperation.CLEAN_INSERT);
	}

	private void insertData(String fileSource, DataType type,
			DatabaseOperation operation) {
		try {
			InputStream sourceStream = new ClassPathResource(fileSource,
					getClass()).getInputStream();
			IDataSet dataset = null;
			if (type == DataType.EXCEL) {
				dataset = new XlsDataSet(sourceStream);
			} else if (type == DataType.FLATXML) {
				dataset = new FlatXmlDataSet(sourceStream);
			}
			operation.execute(
					new DatabaseConnection(DataSourceUtils
							.getConnection(dataSource)), dataset);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void insertXmlData(String fileSource) {
		insertData(fileSource, DataType.FLATXML, DatabaseOperation.INSERT);
	}

	protected void insertXlsData(String fileSource) {
		insertData(fileSource, DataType.EXCEL, DatabaseOperation.INSERT);
	}
}