package de.nikeshkin.postgresreactivelistener.job

import com.fasterxml.jackson.databind.ObjectMapper
import de.nikeshkin.postgresreactivelistener.events.CardEvent
import de.nikeshkin.postgresreactivelistener.events.CardEventPublisher
import de.nikeshkin.postgresreactivelistener.events.CardEventType
import de.nikeshkin.postgresreactivelistener.events.FlagChangedEvent
import de.nikeshkin.postgresreactivelistener.events.LimitChangedEvent
import de.nikeshkin.postgresreactivelistener.repository.CardEventRecordRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

//@Component
class CardEventsReconciliationJob(
    private val cardEventPublisher: CardEventPublisher,
    private val cardEventRecordRepository: CardEventRecordRepository,
    private val jacksonMapper: ObjectMapper
) {

//    @Scheduled(fixedDelay = 10_000)
    fun reconcileCardEvents() {
        println("Reconciliation job started...")

        val unsyncedEvents = cardEventRecordRepository.findUnsyncedEventRecords()

        println("There are ${unsyncedEvents.size} records to reconcile")

        unsyncedEvents.forEach {
            val cardEvent = jacksonMapper.readValue(
                it.eventPayload,
                getEventClass(it.eventType)
            )

            println("Event $cardEvent is gonna be reconciled...")

            cardEventPublisher.publishCardEvent2(cardEvent)


            cardEventRecordRepository.save(it.copy(synced = true))
        }
    }

    private fun getEventClass(eventType: CardEventType): Class<out CardEvent>  {
        return when (eventType) {
            CardEventType.FLAG_CHANGED -> FlagChangedEvent::class.java
            CardEventType.LIMIT_CHANGED -> LimitChangedEvent::class.java
        }
    }
}
