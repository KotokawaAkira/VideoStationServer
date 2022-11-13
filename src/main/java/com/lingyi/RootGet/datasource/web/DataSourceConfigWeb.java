package com.lingyi.RootGet.datasource.web;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * 这个数据源与此项目无关，懒得删，相应的mapper.xml也是同理
 */

@Configuration
@MapperScan(value = "com.lingyi.RootGet.mapper.web",sqlSessionFactoryRef = "sqlSessionFactoryWeb")
public class DataSourceConfigWeb {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.web")
    public DataSourceProperties dataSourcePropertiesWeb(){
        return new DataSourceProperties();
    }
    @Bean
    public DataSource dataSourceWeb(@Qualifier("dataSourcePropertiesWeb")DataSourceProperties dataSourcePropertiesWeb){
        return  dataSourcePropertiesWeb.initializeDataSourceBuilder().build();
    }
    @Bean
    public SqlSessionFactory sqlSessionFactoryWeb(@Qualifier("dataSourceWeb")DataSource dataSourceWeb) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceWeb);
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(patternResolver.getResource("classpath:mybatis/mappers/web/InfoMapper.xml"), patternResolver.getResource("classpath:mybatis/mappers/web/WordsMapper.xml"));
        return sqlSessionFactoryBean.getObject();
    }
    @Bean
    public SqlSessionTemplate sqlSessionTemplateWeb(@Qualifier("sqlSessionFactoryWeb")SqlSessionFactory sqlSessionFactoryWeb){
        return new SqlSessionTemplate(sqlSessionFactoryWeb);
    }
}
