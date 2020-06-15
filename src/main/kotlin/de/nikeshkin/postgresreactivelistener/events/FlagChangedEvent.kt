package de.nikeshkin.postgresreactivelistener.events

import de.nikeshkin.postgresreactivelistener.domain.FlagType

data class FlagChangedEvent(
    val cardId: String,
    val userId: String,
    val enabled: Boolean,
    val flagType: FlagType
) : CardEvent {

    override fun toLogString(): String = this.toString()

}
