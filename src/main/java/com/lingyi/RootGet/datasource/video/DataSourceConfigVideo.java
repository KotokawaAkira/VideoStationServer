package com.lingyi.RootGet.datasource.video;

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

@Configuration
@MapperScan(value = "com.lingyi.RootGet.mapper.video",sqlSessionFactoryRef = "sqlSessionFactoryVideo")
public class DataSourceConfigVideo {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.video")
    public DataSourceProperties dataSourcePropertiesVideo(){
        return new DataSourceProperties();
    }
    @Bean
    public DataSource dataSourceVideo(@Qualifier("dataSourcePropertiesVideo")DataSourceProperties dataSourcePropertiesVideo){
        return dataSourcePropertiesVideo.initializeDataSourceBuilder().build();
    }
    @Bean
    public SqlSessionFactory sqlSessionFactoryVideo(@Qualifier("dataSourceVideo")DataSource dataSourceVideo) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceVideo);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(
                resolver.getResource("classpath:mybatis/mappers/video/AccountMapper.xml"),
                resolver.getResource("classpath:mybatis/mappers/video/VideoMapper.xml"),
                resolver.getResource("classpath:mybatis/mappers/video/CommentMapper.xml"),
                resolver.getResource("classpath:mybatis/mappers/video/CollectionMapper.xml")
                );
        return sqlSessionFactoryBean.getObject();
    }
    @Bean
    public SqlSessionTemplate sqlSessionTemplateVideo(@Qualifier("sqlSessionFactoryVideo")SqlSessionFactory sqlSessionFactoryVideo){
        return new SqlSessionTemplate(sqlSessionFactoryVideo);
    }
}
