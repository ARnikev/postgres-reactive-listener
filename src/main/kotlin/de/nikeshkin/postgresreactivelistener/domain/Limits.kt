package de.nikeshkin.postgresreactivelistener.domain

import org.springframework.data.annotation.Id

data class Limits(
    @Id
    var id: String? = null,
    val type: LimitType,
    val value: Int
)

enum class LimitType {
    DAILY_PAYMENTS, ATM_WITHDRAWALS
}
