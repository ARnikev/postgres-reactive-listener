package de.nikeshkin.postgresreactivelistener.events

import org.springframework.stereotype.Component

@Component
class CardEventPublisher {

    fun publishCardEvent(cardEvent: CardEvent) {
        println("Card event ${cardEvent.toLogString()} has been published")

        if (
            cardEvent.toLogString().contains("MAGSTRIPE_BLOCK") ||
            cardEvent.toLogString().contains("DAILY_PAYMENTS")
        ) {
            throw RuntimeException("Emulating publish event failure")
        }
    }

    fun publishCardEvent2(cardEvent: CardEvent) {
        println("Card event ${cardEvent.toLogString()} has been published")
    }
}
