package com.akitsuki.storage.domain.auth.repository

import com.akitsuki.storage.domain.auth.entity.AccessKey
import java.util.*

interface AccessKeyRepository {
    fun save(accessKey: AccessKey): AccessKey
    fun findById(id: UUID): AccessKey?
    fun findByAccessKeyId(accessKeyId: String): AccessKey?
    fun findByUserId(userId: UUID): List<AccessKey>
    fun findByUserIdAndAccessKeyId(userId: UUID, accessKeyId: String): AccessKey?
    fun existsByAccessKeyId(accessKeyId: String): Boolean
}