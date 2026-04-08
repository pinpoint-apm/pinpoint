package com.navercorp.pinpoint.service.dao.mysql;

import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.service.dao.dto.ServiceParam;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.List;
import java.util.Objects;

public class MysqlServiceRegistryDao implements ServiceRegistryDao {

    private static final String NAMESPACE = ServiceRegistryDao.class.getName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlServiceRegistryDao(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public int insertService(int uid, String name) {
        ServiceParam param = new ServiceParam();
        param.setUid(uid);
        param.setName(name);
        sqlSessionTemplate.insert(NAMESPACE + "insertService", param);
        return param.getUid();
    }

    @Override
    public List<String> selectServiceNames() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectServiceNames");
    }

    @Override
    public List<ServiceEntity> selectServiceList(int limit) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectServiceList", limit);
    }

    @Override
    public ServiceEntity selectService(String name) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectServiceByName", name);
    }

    @Override
    public ServiceEntity selectService(int uid) {
        return sqlSessionTemplate.selectOne(NAMESPACE + "selectServiceByUid", uid);
    }

    @Override
    public void deleteService(int uid) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteService", uid);
    }
}
