package de.nikeshkin.postgresreactivelistener.listener

import org.postgresql.PGConnection
import org.postgresql.PGProperty
import org.postgresql.replication.LogSequenceNumber
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy


@Component
class WalChangesListener {

    val executorService = Executors.newFixedThreadPool(1)

    @PostConstruct
    fun listen() {
        executorService.submit {
            val url = "jdbc:postgresql://localhost:5432/postgres"
            val props = Properties()

            PGProperty.USER[props] = "postgres"
            PGProperty.PASSWORD[props] = "docker"
            PGProperty.ASSUME_MIN_SERVER_VERSION[props] = "9.4"
            PGProperty.REPLICATION[props] = "database"
            PGProperty.PREFER_QUERY_MODE[props] = "simple"

            val con: Connection = DriverManager.getConnection(url, props)
            val replConnection: PGConnection = con.unwrap(PGConnection::class.java)

            var stream = replConnection.replicationAPI
                .replicationStream()
                .logical()
                .withSlotName("demo_logical_slot")
                .withSlotOption("include-xids", false)
                .withSlotOption("skip-empty-xacts", true)
                .withStatusInterval(20, TimeUnit.SECONDS)
                .start()

            while (true) { //non blocking receive message
                val msg: ByteBuffer? = stream.readPending()

                if (msg == null) {
                    TimeUnit.MILLISECONDS.sleep(10L)
                    continue
                }

                val offset: Int = msg.arrayOffset()
                val source: ByteArray = msg.array()
                val length = source.size - offset

                val walRecord = String(source, offset, length)

                println("Last receive LSN = ${stream.lastReceiveLSN}")
                println("Last applied LSN = ${stream.lastAppliedLSN}")

                try {
                    println("WAL record received:")
                    println(walRecord)

                    if (walRecord.contains("MAGSTRIPE_BLOCK")) {
                        stream.setAppliedLSN(stream.lastAppliedLSN)
                        stream.setFlushedLSN(stream.lastAppliedLSN)
                        stream.forceUpdateStatus()

                        throw IllegalStateException("Testing wal record consumtion failure")
                    }

                    //feedback
                    stream.setAppliedLSN(stream.lastReceiveLSN)
                    stream.setFlushedLSN(stream.lastReceiveLSN)
                } catch (e: Exception) {
                    println("failed to consume wal log record $walRecord")
                    println(e.localizedMessage)
                }
            }
        }
    }

//    @PreDestroy
    fun onShutDown() {
        executorService.shutdownNow()
    }
}
