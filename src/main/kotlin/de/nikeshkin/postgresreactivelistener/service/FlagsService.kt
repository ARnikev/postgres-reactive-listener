package de.nikeshkin.postgresreactivelistener.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.nikeshkin.postgresreactivelistener.domain.Flag
import de.nikeshkin.postgresreactivelistener.events.CardEventPublisher
import de.nikeshkin.postgresreactivelistener.events.CardEventRecord
import de.nikeshkin.postgresreactivelistener.events.CardEventType
import de.nikeshkin.postgresreactivelistener.events.FlagChangedEvent
import de.nikeshkin.postgresreactivelistener.repository.CardEventRecordRepository
import de.nikeshkin.postgresreactivelistener.repository.FlagsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class FlagsService(
    private val flagsRepository: FlagsRepository,
    private val cardEventRecordRepository: CardEventRecordRepository,
    private val transactionTemplate: TransactionTemplate,
    private val jacksonMapper: ObjectMapper,
    private val cardEventPublisher: CardEventPublisher
) {

    fun addFlag(flag: Flag) {
        val flagChangedEvent = FlagChangedEvent(
            cardId = UUID.randomUUID().toString(),
            userId = UUID.randomUUID().toString(),
            enabled = flag.enabled,
            flagType = flag.type
        )

        val cardEventRecord = CardEventRecord(
            synced = false,
            eventPayload = jacksonMapper.writeValueAsString(flagChangedEvent),
            eventType = CardEventType.FLAG_CHANGED
        )

        transactionTemplate.execute {
            flagsRepository.save(flag)
            cardEventRecordRepository.save(cardEventRecord)
        }
    }

    // problems:
    // - event can potentially be reconciled before published inside this method
    // - while we reconciling some old event some new event comes and being published during this sync flow,
    //   but then we finally publish outdated old event during reconciliation job
    fun addFlagWithReconciliation(flag: Flag) {
        val flagChangedEvent = FlagChangedEvent(
            cardId = UUID.randomUUID().toString(),
            userId = UUID.randomUUID().toString(),
            enabled = flag.enabled,
            flagType = flag.type
        )

        var cardEventRecord = CardEventRecord(
            synced = false,
            eventPayload = jacksonMapper.writeValueAsString(flagChangedEvent),
            eventType = CardEventType.FLAG_CHANGED
        )

        transactionTemplate.execute {
            flagsRepository.save(flag)
            cardEventRecord = cardEventRecordRepository.save(cardEventRecord)
        }

        // to not allow the reconciliation job processing this event we can do these 2 operations with select for update
        // locking this particular event in the DB
        cardEventPublisher.publishCardEvent(flagChangedEvent)
        cardEventRecordRepository.save(cardEventRecord.copy(synced = true))

    }

    fun updateFlag(flag: Flag) {
        transactionTemplate.execute {
            val flagChangedEvent = FlagChangedEvent(
                cardId = UUID.randomUUID().toString(),
                userId = UUID.randomUUID().toString(),
                enabled = flag.enabled,
                flagType = flag.type
            )

            flagsRepository.save(flag)
            cardEventRecordRepository.save(
                CardEventRecord(
                    synced = false,
                    eventPayload = jacksonMapper.writeValueAsString(flagChangedEvent),
                    eventType = CardEventType.FLAG_CHANGED
                )
            )
        }
    }
}
