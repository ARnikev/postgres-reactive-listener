package de.nikeshkin.postgresreactivelistener.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.nikeshkin.postgresreactivelistener.domain.Limits
import de.nikeshkin.postgresreactivelistener.events.CardEventPublisher
import de.nikeshkin.postgresreactivelistener.events.CardEventRecord
import de.nikeshkin.postgresreactivelistener.events.CardEventType
import de.nikeshkin.postgresreactivelistener.events.LimitChangedEvent
import de.nikeshkin.postgresreactivelistener.repository.CardEventRecordRepository
import de.nikeshkin.postgresreactivelistener.repository.LimitsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class LimitsService(
    private val limitsRepository: LimitsRepository,
    private val cardEventRecordRepository: CardEventRecordRepository,
    private val transactionTemplate: TransactionTemplate,
    private val jacksonMapper: ObjectMapper,
    private val cardEventPublisher: CardEventPublisher
) {

    fun addLimit(limit: Limits) {
        transactionTemplate.execute {
            val limitChangedEvent = LimitChangedEvent(
                cardId = UUID.randomUUID().toString(),
                userId = UUID.randomUUID().toString(),
                limitType = limit.type,
                limitValue = limit.value
            )

            limitsRepository.save(limit)
            cardEventRecordRepository.save(
                CardEventRecord(
                    synced = false,
                    eventPayload = jacksonMapper.writeValueAsString(limitChangedEvent),
                    eventType = CardEventType.LIMIT_CHANGED
                )
            )
        }
    }

    fun addLimitWithReconciliation(limit: Limits) {
        val limitChangedEvent = LimitChangedEvent(
            cardId = UUID.randomUUID().toString(),
            userId = UUID.randomUUID().toString(),
            limitType = limit.type,
            limitValue = limit.value
        )

        var cardEventRecord = CardEventRecord(
            synced = false,
            eventPayload = jacksonMapper.writeValueAsString(limitChangedEvent),
            eventType = CardEventType.LIMIT_CHANGED
        )

        transactionTemplate.execute {
            limitsRepository.save(limit)
            cardEventRecord = cardEventRecordRepository.save(cardEventRecord)
        }

        cardEventPublisher.publishCardEvent(limitChangedEvent)

        cardEventRecordRepository.save(cardEventRecord.copy(synced = true))

    }

    fun updateLimit(limit: Limits) {
        transactionTemplate.execute {
            val limitChangedEvent = LimitChangedEvent(
                cardId = UUID.randomUUID().toString(),
                userId = UUID.randomUUID().toString(),
                limitType = limit.type,
                limitValue = limit.value
            )

            limitsRepository.save(limit)
            cardEventRecordRepository.save(
                CardEventRecord(
                    synced = false,
                    eventPayload = jacksonMapper.writeValueAsString(limitChangedEvent),
                    eventType = CardEventType.LIMIT_CHANGED
                )
            )
        }
    }
}
