package de.nikeshkin.postgresreactivelistener.events

import de.nikeshkin.postgresreactivelistener.domain.LimitType

data class LimitChangedEvent(
    val cardId: String,
    val userId: String,
    val limitType: LimitType,
    val limitValue: Int
) : CardEvent {
    override fun toLogString(): String = this.toString()
}
