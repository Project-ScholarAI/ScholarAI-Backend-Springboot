package dev.project.scholar_ai.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Binds properties under spring.datasource.paper.*
 * Builds:
 * - DataSource bean named "paperDataSource"
 * - LocalContainerEntityManagerFactoryBean named "paperEntityManagerFactory"
 * - JpaTransactionManager named "paperTransactionManager"
 *
 * Scans:
 * - Entities in dev.project.scholar_ai.model.paper
 * - Repositories in dev.project.scholar_ai.repository.paper
 */
@Configuration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "dev.project.scholar_ai.repository.paper",
        entityManagerFactoryRef = "paperEntityManagerFactory",
        transactionManagerRef = "paperTransactionManager")
public class PaperDataSourceConfig {

    /**
     * 1) Bind to spring.datasource.paper.* (URL, user, pass, driver, hikari)
     */
    @Bean
    @ConfigurationProperties("spring.datasource.paper")
    public DataSourceProperties paperDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * 2) Build the actual DataSource (HikariCP by default) for paperDB
     */
    @Bean(name = "paperDataSource")
    public DataSource paperDataSource() {
        return paperDataSourceProperties().initializeDataSourceBuilder().build();
    }

    /**
     * 3) Create a LocalContainerEntityManagerFactoryBean (JPA) for paperDB
     * — Scans entity package: dev.project.scholar_ai.model.paper
     * — Gives it a persistence unit name "paper"
     */
    @Bean(name = "paperEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean paperEntityManagerFactory(
            @Qualifier("paperDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("dev.project.scholar_ai.model.paper");
        factory.setPersistenceUnitName("paper");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        vendorAdapter.setShowSql(true);
        factory.setJpaVendorAdapter(vendorAdapter);

        // Set JPA properties manually based on application-dev.yml configuration
        Map<String, Object> jpaPropertiesMap = new HashMap<>();
        jpaPropertiesMap.put("hibernate.hbm2ddl.auto", "update");
        jpaPropertiesMap.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaPropertiesMap.put("hibernate.show_sql", true);
        jpaPropertiesMap.put("hibernate.format_sql", true);
        jpaPropertiesMap.put("hibernate.use_sql_comments", true);

        factory.setJpaPropertyMap(jpaPropertiesMap);

        return factory;
    }

    /**
     * 4) Create a JpaTransactionManager for paperDB's EntityManagerFactory
     */
    @Bean(name = "paperTransactionManager")
    public PlatformTransactionManager paperTransactionManager(
            @Qualifier("paperEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
