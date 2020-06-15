package de.nikeshkin.postgresreactivelistener.events

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id

data class CardEventRecord(
    @Id
    var id: String? = null,
    @JsonProperty("event_type")
    val eventType: CardEventType,
    val synced: Boolean,
    @JsonProperty("event_payload")
    val eventPayload: String
)

enum class CardEventType {
    FLAG_CHANGED, LIMIT_CHANGED;
}
