package com.akitsuki.storage.domain.auth.model

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class AccessKeyCreationCommand(
    val userId: UUID,
    val description: String? = null,
    val expiresInDays: Int? = null
) {
    fun calculateExpiresAt(): Instant? {
        return expiresInDays?.let {
            Instant.now().plus(it.toLong(), ChronoUnit.DAYS)
        }
    }
}