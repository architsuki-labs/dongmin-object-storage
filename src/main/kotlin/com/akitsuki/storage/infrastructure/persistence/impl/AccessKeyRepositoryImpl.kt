package com.akitsuki.storage.infrastructure.persistence.impl

import com.akitsuki.storage.domain.auth.entity.AccessKey
import com.akitsuki.storage.domain.auth.repository.AccessKeyRepository
import com.akitsuki.storage.infrastructure.persistence.jpa.AccessKeyJpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class AccessKeyRepositoryImpl(
    private val jpaRepository: AccessKeyJpaRepository
) : AccessKeyRepository {
    
    override fun save(accessKey: AccessKey): AccessKey {
        return jpaRepository.save(accessKey)
    }
    
    override fun findById(id: UUID): AccessKey? {
        return jpaRepository.findById(id).orElse(null)
    }
    
    override fun findByAccessKeyId(accessKeyId: String): AccessKey? {
        return jpaRepository.findByAccessKeyId(accessKeyId)
    }
    
    override fun findByUserId(userId: UUID): List<AccessKey> {
        return jpaRepository.findByUserId(userId)
    }
    
    override fun findByUserIdAndAccessKeyId(userId: UUID, accessKeyId: String): AccessKey? {
        return jpaRepository.findByUserIdAndAccessKeyId(userId, accessKeyId)
    }
    
    override fun existsByAccessKeyId(accessKeyId: String): Boolean {
        return jpaRepository.existsByAccessKeyId(accessKeyId)
    }
}