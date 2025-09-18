package com.akitsuki.storage.infrastructure.persistence.jpa

import com.akitsuki.storage.domain.auth.entity.AccessKey
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AccessKeyJpaRepository : JpaRepository<AccessKey, UUID> {
    fun findByAccessKeyId(accessKeyId: String): AccessKey?
    fun findByUserId(userId: UUID): List<AccessKey>
    fun findByUserIdAndAccessKeyId(userId: UUID, accessKeyId: String): AccessKey?
    fun existsByAccessKeyId(accessKeyId: String): Boolean
}