package com.akitsuki.storage.domain.auth.model

import com.akitsuki.storage.domain.auth.entity.AccessKey
import com.akitsuki.storage.domain.auth.enums.AccessKeyStatus
import java.time.Instant
import java.util.*

data class AccessKeyCreationResult(
    val accessKeyId: String,
    val secretKey: String,
    val status: AccessKeyStatus,
    val createdAt: Instant,
    val expiresAt: Instant? = null
) {
    companion object {
        fun from(accessKey: AccessKey, secretKey: String): AccessKeyCreationResult {
            return AccessKeyCreationResult(
                accessKeyId = accessKey.accessKeyId,
                secretKey = secretKey,
                status = accessKey.getStatus(),
                createdAt = accessKey.createdAt,
                expiresAt = accessKey.expiresAt
            )
        }
    }
}