package de.nikeshkin.postgresreactivelistener.listener

import com.fasterxml.jackson.databind.ObjectMapper
import de.nikeshkin.postgresreactivelistener.events.CardEvent
import de.nikeshkin.postgresreactivelistener.events.CardEventPublisher
import de.nikeshkin.postgresreactivelistener.events.CardEventRecord
import de.nikeshkin.postgresreactivelistener.events.CardEventType
import de.nikeshkin.postgresreactivelistener.events.FlagChangedEvent
import de.nikeshkin.postgresreactivelistener.events.LimitChangedEvent
import de.nikeshkin.postgresreactivelistener.repository.CardEventRecordRepository
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.postgresql.api.PostgresqlReplicationConnection
import io.r2dbc.postgresql.api.PostgresqlResult
import io.r2dbc.postgresql.replication.LogSequenceNumber
import io.r2dbc.postgresql.replication.ReplicationRequest
import io.r2dbc.postgresql.replication.ReplicationSlotRequest
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


//@Component
class CardEventRecordNotificationListener(
    private val jacksonMapper: ObjectMapper,
    private val cardEventRecordRepository: CardEventRecordRepository,
    private val cardEventPublisher: CardEventPublisher
) {

    private final val connectionFactory: PostgresqlConnectionFactory =
        ConnectionFactories.get("r2dbc:postgres://postgres:docker@localhost:5432/postgres") as PostgresqlConnectionFactory

    private final val connection = Mono.from(connectionFactory.create())
        .cast(PostgresqlConnection::class.java).block()

//    @PostConstruct
    fun listenNotifications() {
        connection!!.createStatement("LISTEN card_event_notification")
            .execute()
            .flatMap(PostgresqlResult::getRowsUpdated)
            .subscribe()

        connection.notifications
            .map {
                try {
                    val cardEventRecordNotification = it.parameter

                    println("CardEventRecordNotification = $cardEventRecordNotification")

                    if (cardEventRecordNotification != null) {
                        val cardEventRecord = jacksonMapper.readValue(
                            cardEventRecordNotification,
                            CardEventRecord::class.java
                        )

                        val cardEvent = jacksonMapper.readValue(
                            cardEventRecord.eventPayload,
                            getEventClass(cardEventRecord.eventType)
                        )

                        cardEventPublisher.publishCardEvent(cardEvent)

                        cardEventRecordRepository.save(cardEventRecord.copy(synced = true))
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)

                    throw e
                }
            }
            .doOnError { println("Got an error!!") }
            .subscribe()
    }

//    @PostConstruct
    fun listenWalChanges() {
        val replicationMono: Mono<PostgresqlReplicationConnection> = connectionFactory.replication()
        val connection = replicationMono.cast(PostgresqlReplicationConnection::class.java).block()

        val replicationRequest = ReplicationRequest.logical()
            .slotName("demo_logical_slot")
            .startPosition(LogSequenceNumber.valueOf(0))
            .slotOption("skip-empty-xacts", true)
            .slotOption("include-xids", false)
            .build()

        connection!!.startReplication(replicationRequest)
            .flatMapMany {
                it.map {
                    byteBuff -> {
                        val offset: Int = byteBuff.arrayOffset()
                        val source: ByteArray = byteBuff.array()
                        val length = source.size - offset

                        val walRecord = String(source, offset, length)

                        println("walRecord = $walRecord")
                    }
                }
            }.subscribe()
    }


    private fun getEventClass(eventType: CardEventType): Class<out CardEvent>  {
        return when (eventType) {
            CardEventType.FLAG_CHANGED -> FlagChangedEvent::class.java
            CardEventType.LIMIT_CHANGED -> LimitChangedEvent::class.java
        }
    }

    // concerns:
    // what if connection drops?
    // if error happens, we retry several times and still fail -> we will loose the value
    // we need only one instance of our application to be able to listen for such events
    // during deployment we can have duplicated events being published

    @PreDestroy
    private fun preDestroy() {
        connection!!.close().subscribe()
    }
}
