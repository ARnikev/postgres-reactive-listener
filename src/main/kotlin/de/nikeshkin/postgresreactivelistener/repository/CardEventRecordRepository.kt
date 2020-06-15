package de.nikeshkin.postgresreactivelistener.repository

import de.nikeshkin.postgresreactivelistener.events.CardEventRecord
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface CardEventRecordRepository : CrudRepository<CardEventRecord, String> {

    @Query("SELECT * FROM card_event_record WHERE synced = false")
    fun findUnsyncedEventRecords(): List<CardEventRecord>
}
