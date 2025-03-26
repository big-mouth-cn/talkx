package org.bigmouth.gpt.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author allen
 * @date 2023/6/13
 * @since 1.0.0
 */
@Configuration
public class MyBatisPlusAutoConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }

//    @Bean
//    @Primary
//    @ConditionalOnMissingBean
//    public DynamicDataSourceProvider dynamicDataSourceProvider() {
//        Map<String, DataSourceProperty> datasource = this.properties.getDatasource();
//        return new YmlDynamicDataSourceProvider(datasource);
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public DynamicRoutingDataSource dynamicRoutingDataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
//        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
//        dynamicRoutingDataSource.setPrimary(this.properties.getPrimary());
//        dynamicRoutingDataSource.setStrict(this.properties.getStrict());
//        dynamicRoutingDataSource.setStrategy(this.properties.getStrategy());
//        dynamicRoutingDataSource.setProvider(dynamicDataSourceProvider);
//        dynamicRoutingDataSource.setP6spy(this.properties.getP6spy());
//        dynamicRoutingDataSource.setSeata(this.properties.getSeata());
//        return dynamicRoutingDataSource;
//    }
}
