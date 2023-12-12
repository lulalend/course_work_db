package com.arslanka.blablacar2.configs

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.ConnectionProvider
import org.jooq.DSLContext
import org.jooq.ExecuteListener
import org.jooq.SQLDialect
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.jooq.impl.DefaultExecuteListenerProvider
import org.jooq.impl.DefaultTransactionProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.jooq.JooqExceptionTranslator
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(DataSourceProperties::class)
class DbConfig {

    @Bean
    fun dataSource(
        @Value("\${spring.datasource.url}") url: String,
        @Value("\${spring.datasource.username}") userName: String,
        @Value("\${spring.datasource.password}") password: String,
        @Value("\${spring.datasource.driverClassName}") driverClassName: String,
        @Value("\${spring.datasource.hikari.maximum-pool-size}") maximumPoolSize: Int,
        @Value("\${spring.datasource.hikari.minimum-idle}") minimumIdle: Int,
    ): DataSource {
        val config = HikariConfig()
        config.driverClassName = driverClassName
        config.username = userName
        config.password = password
        config.jdbcUrl = url
        config.maximumPoolSize = maximumPoolSize
        config.minimumIdle = minimumIdle

        return HikariDataSource(config)
    }

    @Bean
    fun connectionProvider(dataSource: DataSource): ConnectionProvider = DataSourceConnectionProvider(dataSource)

    @Bean
    fun jooqExceptionTranslator(): ExecuteListener = JooqExceptionTranslator()

    @Bean
    fun jooqDefaultConfiguration(
        connectionProvider: ConnectionProvider,
        jooqExceptionTranslator: ExecuteListener,
    ): DefaultConfiguration =
        DefaultConfiguration().apply {
            set(connectionProvider)
            set(DefaultExecuteListenerProvider(jooqExceptionTranslator))
            set(DefaultTransactionProvider(connectionProvider))
            set(SQLDialect.POSTGRES)
        }

    @Bean
    fun dslContext(jooqDefaultConfiguration: org.jooq.Configuration): DSLContext =
        DefaultDSLContext(jooqDefaultConfiguration)
}