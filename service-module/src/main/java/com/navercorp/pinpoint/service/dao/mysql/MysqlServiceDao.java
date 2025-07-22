package com.navercorp.pinpoint.service.dao.mysql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.dao.ServiceDao;
import com.navercorp.pinpoint.service.vo.ServiceEntry;
import com.navercorp.pinpoint.service.vo.ServiceInfo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@ConditionalOnProperty(name = "pinpoint.modules.service.dao.type", havingValue = "mysql")
public class MysqlServiceDao implements ServiceDao {
    private static final String NAMESPACE = ServiceDao.class.getName() + ".";

    private final Mapper mapper;
    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlServiceDao(ObjectMapper objectMapper,
                           @Qualifier("serviceMysqlSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.mapper = new Mapper(Objects.requireNonNull(objectMapper, "objectMapper"));
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public int insertService(int uid, String name, Map<String, String> configuration) {
        ServiceParam serviceParam = new ServiceParam();
        serviceParam.setUid(uid);
        serviceParam.setName(name);
        serviceParam.setConfiguration(mapper.toJson(configuration));
        sqlSessionTemplate.insert(NAMESPACE + "insertService", serviceParam);
        return serviceParam.getUid();
    }

    @Override
    public List<String> selectServiceNames() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectServiceNameList");
    }

    @Override
    public List<ServiceEntry> selectServiceList(int limit) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectServiceEntryList", limit);
    }

    @Override
    public ServiceInfo selectServiceInfo(String name) {
        ServiceParam serviceParam = sqlSessionTemplate.selectOne(NAMESPACE + "selectServiceInfo", name);
        if (serviceParam == null) {
            return null;
        }
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setUid(serviceParam.getUid());
        serviceInfo.setName(serviceParam.getName());
        serviceInfo.setConfiguration(mapper.fromJson(serviceParam.getConfiguration()));
        return serviceInfo;
    }

    @Override
    public ServiceEntry selectServiceEntry(String name) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectServiceEntryByName", name);
    }

    @Override
    public ServiceEntry selectServiceEntry(int uid) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectServiceEntryByUid", uid);
    }

    @Override
    public void updateServiceConfig(int uid, Map<String, String> configuration) {
        ServiceParam serviceParam = new ServiceParam();
        serviceParam.setUid(uid);
        serviceParam.setConfiguration(mapper.toJson(configuration));
        sqlSessionTemplate.update(NAMESPACE + "updateServiceConfig", serviceParam);
    }


    @Override
    public void updateServiceName(int uid, String name) {
        ServiceParam serviceParam = new ServiceParam();
        serviceParam.setUid(uid);
        serviceParam.setName(name);
        sqlSessionTemplate.update(NAMESPACE + "updateServiceName", serviceParam);
    }

    @Override
    public void deleteService(int uid) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteService", uid);
    }

    static class Mapper {
        private static final String EMPTY_MAP_JSON = "{}";

        private final ObjectMapper mapper;

        Mapper(ObjectMapper objectMapper) {
            this.mapper = Objects.requireNonNull(objectMapper, "objectMapper");
        }

        public String toJson(Map<String, String> configuration) {
            if (configuration == null || configuration.isEmpty()) {
                return EMPTY_MAP_JSON;
            }
            try {
                return mapper.writeValueAsString(configuration);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert configuration to JSON", e);
            }
        }

        public Map<String, String> fromJson(String json) {
            if (json == null || json.isBlank()) {
                return Collections.emptyMap();
            }
            try {
                return mapper.readValue(json, new TypeReference<Map<String, String>>() {
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert JSON to configuration", e);
            }
        }
    }

    public static class ServiceParam {

        private int uid;
        private String name;
        private String configuration;

        public ServiceParam() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public String getConfiguration() {
            return configuration;
        }

        public void setConfiguration(String configuration) {
            this.configuration = configuration;
        }
    }
}
