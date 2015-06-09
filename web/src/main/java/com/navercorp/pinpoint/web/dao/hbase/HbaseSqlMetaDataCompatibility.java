package com.navercorp.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.common.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;

public class HbaseSqlMetaDataCompatibility implements SqlMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final boolean SQL_METADATA_VER2_EXISTED;
    private final boolean SQL_METADATA_EXISTED;

//    @Autowired
    private SqlMetaDataDao hbaseSqlMetaDataPastVersionDao;
    
//    @Autowired
    private SqlMetaDataDao hbaseSqlMetaDataDao;

    
    @Autowired
    public HbaseSqlMetaDataCompatibility(HBaseAdminTemplate hBaseAdminTemplate) {
        SQL_METADATA_VER2_EXISTED = hBaseAdminTemplate.tableExists(HBaseTables.SQL_METADATA_VER2);
        
        if (SQL_METADATA_VER2_EXISTED == false) {
            logger.warn("Please create 'SqlMetaData_Ver2' table.");
        }
        
        SQL_METADATA_EXISTED = hBaseAdminTemplate.tableExists(HBaseTables.SQL_METADATA);
        
        if (SQL_METADATA_EXISTED == true) {
            logger.warn("SqlMetaData table exists. Recommend that only use SqlMetaData_Ver2 table.");
        }
        
        if(SQL_METADATA_EXISTED == false && SQL_METADATA_VER2_EXISTED == false) {
            throw new RuntimeException("Please check for sqlMetaData_ver2 table in HBase. Need to create 'SqlMetaData_Ver2' table.");
        }
    }
    
    @Override
    public List<SqlMetaDataBo> getSqlMetaData(String agentId, long time, int hashCode) {
        List<SqlMetaDataBo> sqlMetaDataList = new ArrayList<SqlMetaDataBo>();
        
        if (SQL_METADATA_VER2_EXISTED) {
            sqlMetaDataList = hbaseSqlMetaDataDao.getSqlMetaData(agentId, time, hashCode);
            
            if (sqlMetaDataList.size() >= 1) {
                return sqlMetaDataList;
            }
        } 
        
        if(SQL_METADATA_EXISTED) {
            sqlMetaDataList = hbaseSqlMetaDataPastVersionDao.getSqlMetaData(agentId, time, hashCode);
        }
    
        return sqlMetaDataList;
    }
    
    public void setHbaseSqlMetaDataDao(SqlMetaDataDao hbaseSqlMetaDataDao) {
        this.hbaseSqlMetaDataDao = hbaseSqlMetaDataDao;
    }
    
    public void setHbaseSqlMetaDataPastVersionDao(SqlMetaDataDao hbaseSqlMetaDataPastVersionDao) {
        this.hbaseSqlMetaDataPastVersionDao = hbaseSqlMetaDataPastVersionDao;
    }

}
