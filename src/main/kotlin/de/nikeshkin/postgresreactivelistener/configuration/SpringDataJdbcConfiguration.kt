package de.nikeshkin.postgresreactivelistener.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.nikeshkin.postgresreactivelistener.domain.Flag
import de.nikeshkin.postgresreactivelistener.domain.Limits
import de.nikeshkin.postgresreactivelistener.events.CardEventRecord
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.UUID
import javax.sql.DataSource


@Configuration
@EnableJdbcRepositories("de.nikeshkin.postgresreactivelistener.repository")
class SpringDataJdbcConfiguration : AbstractJdbcConfiguration() {

    @Bean
    fun idSetting(): ApplicationListener<*>? {
        return ApplicationListener { event: BeforeSaveEvent<*> ->
            if (event.entity is Flag) {
                val flag = event.entity as Flag

                if (flag.id == null) {
                    flag.id = UUID.randomUUID().toString()
                }
            }

            if (event.entity is Limits) {
                val limit = event.entity as Limits

                if (limit.id == null) {
                    limit.id = UUID.randomUUID().toString()
                }
            }

            if (event.entity is CardEventRecord) {
                val cardEventRecord = event.entity as CardEventRecord

                if (cardEventRecord.id == null) {
                    cardEventRecord.id = UUID.randomUUID().toString()
                }
            }
        }
    }

    @Bean
    fun dataSource(): DataSource {
        val hikariConfig = HikariConfig()

        with(hikariConfig) {
            jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"
            username = "postgres"
            password = "docker"
            connectionTimeout = 1000
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            maximumPoolSize = 10
        }

        return HikariDataSource(hikariConfig)    }

    @Bean
    fun namedParameterJdbcOperations(dataSource: DataSource): NamedParameterJdbcOperations {
        return NamedParameterJdbcTemplate(dataSource)
    }
}
