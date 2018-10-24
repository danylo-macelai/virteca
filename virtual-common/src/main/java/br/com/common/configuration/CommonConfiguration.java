package br.com.common.configuration;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import br.com.common.utils.Utils;

@EnableTransactionManagement
public abstract class CommonConfiguration {

    private static final String[] PACKAGES                  = { "br.com.common" };

    private static final String   DB_DRIVER_CLASS           = "db.driver";
    private static final String   DB_PASSWORD               = "db.password";
    private static final String   DB_URL                    = "db.url";
    private static final String   DB_USER                   = "db.username";
    private static final String   DB_VALIDATION_QUERY       = "db.validation.query";

    private static final String   HIBERNATE_DIALECT         = "hibernate.dialect";
    private static final String   HIBERNATE_FORMAT_SQL      = "hibernate.format_sql";
    private static final String   HIBERNATE_HBM2DDL_AUTO    = "hibernate.hbm2ddl.auto";
    private static final String   HIBERNATE_NAMING_STRATEGY = "hibernate.ejb.naming_strategy";
    private static final String   HIBERNATE_SHOW_SQL        = "hibernate.show_sql";

    protected abstract String[] packagesToScan();

    @Bean(destroyMethod = "close")
    DataSource dataSource(Environment env) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUsername(env.getRequiredProperty(DB_USER));
        dataSource.setPassword(env.getRequiredProperty(DB_PASSWORD));
        dataSource.setUrl(env.getRequiredProperty(DB_URL));
        dataSource.setDriverClassName(env.getRequiredProperty(DB_DRIVER_CLASS));

        dataSource.setInitialSize(20);
        dataSource.setMaxActive(20);
        dataSource.setMaxIdle(8);
        dataSource.setMinIdle(4);
        dataSource.setMaxWait(30000);

        dataSource.setValidationQuery(env.getProperty(DB_VALIDATION_QUERY));
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(5000);
        dataSource.setMinEvictableIdleTimeMillis(4000);
        return dataSource;
    }

    @Bean
    PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
        return new PersistenceAnnotationBeanPostProcessor();
    }

    @Bean
    JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment env) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan(Utils.concat(packagesToScan(), PACKAGES));

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();
        jpaProperties.put(HIBERNATE_DIALECT, env.getRequiredProperty(HIBERNATE_DIALECT));
        jpaProperties.put(HIBERNATE_HBM2DDL_AUTO, env.getRequiredProperty(HIBERNATE_HBM2DDL_AUTO));
        jpaProperties.put(HIBERNATE_NAMING_STRATEGY, env.getRequiredProperty(HIBERNATE_NAMING_STRATEGY));
        jpaProperties.put(HIBERNATE_SHOW_SQL, env.getRequiredProperty(HIBERNATE_SHOW_SQL));
        jpaProperties.put(HIBERNATE_FORMAT_SQL, env.getRequiredProperty(HIBERNATE_FORMAT_SQL));
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        return entityManagerFactoryBean;
    }

}
