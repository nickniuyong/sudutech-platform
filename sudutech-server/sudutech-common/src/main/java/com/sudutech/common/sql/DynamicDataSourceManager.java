package com.sudutech.common.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源管理：按数据源ID缓存 HikariCP 连接池
 */
@Slf4j
@Component
public class DynamicDataSourceManager implements DisposableBean {

    private final Map<Long, HikariDataSource> poolCache = new ConcurrentHashMap<>();
    private final Map<Long, String> urlCache = new ConcurrentHashMap<>();

    /**
     * 获取（或创建）指定数据源的连接池
     */
    public synchronized HikariDataSource getPool(Long datasourceId, String jdbcUrl,
                                                 String username, String password, String driverClass) {
        HikariDataSource exist = poolCache.get(datasourceId);
        if (exist != null && jdbcUrl.equals(urlCache.get(datasourceId)) && !exist.isClosed()) {
            return exist;
        }
        if (exist != null) {
            // 连接信息变更或已关闭，重建
            exist.close();
            poolCache.remove(datasourceId);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClass);
        config.setPoolName("dynamic-ds-" + datasourceId);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(3);
        config.setConnectionTimeout(8000);
        config.setValidationTimeout(5000);
        config.setMaxLifetime(1200000);
        Properties props = new Properties();
        props.setProperty("characterEncoding", "utf8");
        config.setDataSourceProperties(props);
        HikariDataSource ds = new HikariDataSource(config);
        poolCache.put(datasourceId, ds);
        urlCache.put(datasourceId, jdbcUrl);
        log.info("创建动态数据源连接池 ds={} url={}", datasourceId, jdbcUrl);
        return ds;
    }

    public void remove(Long datasourceId) {
        HikariDataSource ds = poolCache.remove(datasourceId);
        if (ds != null) {
            ds.close();
        }
    }

    @Override
    public void destroy() {
        poolCache.values().forEach(HikariDataSource::close);
        poolCache.clear();
    }
}
