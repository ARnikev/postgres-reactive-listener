package de.nikeshkin.postgresreactivelistener.domain

import org.springframework.data.annotation.Id

data class Flag(
    @Id
    var id: String? = null,
    val type: FlagType,
    val enabled: Boolean
)

enum class FlagType {
    PAYMENTS_ABROAD,
    ONLINE_PAYMENTS,
    ATM_WITHDRAWALS,
    GAMBLING_BLOCK,
    MAGSTRIPE_BLOCK;
}
