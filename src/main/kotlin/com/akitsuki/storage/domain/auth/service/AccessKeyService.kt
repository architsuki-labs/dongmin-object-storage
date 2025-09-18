package com.akitsuki.storage.domain.auth.service

import com.akitsuki.storage.config.AccessKeyProperties
import com.akitsuki.storage.domain.auth.entity.AccessKey
import com.akitsuki.storage.domain.auth.model.AccessKeyCreationCommand
import com.akitsuki.storage.domain.auth.model.AccessKeyCreationResult
import com.akitsuki.storage.domain.auth.repository.AccessKeyRepository
import com.akitsuki.storage.domain.user.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class AccessKeyService(
    private val accessKeyRepository: AccessKeyRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val accessKeyProperties: AccessKeyProperties
) {
    private val secureRandom = SecureRandom()
    
    fun createAccessKey(command: AccessKeyCreationCommand): AccessKeyCreationResult {
        // 사용자 존재 확인
        userRepository.findById(command.userId)
            ?: throw IllegalArgumentException("User not found with id: ${command.userId}")
        
        // 액세스 키 ID 생성
        val accessKeyIdBuilder = StringBuilder(accessKeyProperties.prefix)
        repeat(accessKeyProperties.keyLength) {
            accessKeyIdBuilder.append(
                accessKeyProperties.allowedChars[secureRandom.nextInt(accessKeyProperties.allowedChars.length)]
            )
        }
        val accessKeyId = accessKeyIdBuilder.toString()
        
        // 시크릿 키 생성
        val secretKeyBuilder = StringBuilder()
        repeat(accessKeyProperties.secretLength) {
            secretKeyBuilder.append(
                accessKeyProperties.secretChars[secureRandom.nextInt(accessKeyProperties.secretChars.length)]
            )
        }
        val secretKey = secretKeyBuilder.toString()
        
        val secretKeyHash = passwordEncoder.encode(secretKey)
        
        // 엔티티 생성 및 저장
        val accessKey = AccessKey.create(
            userId = command.userId,
            accessKeyId = accessKeyId,
            secretKeyHash = secretKeyHash,
            expiresAt = command.calculateExpiresAt()
        )
        
        val savedKey = accessKeyRepository.save(accessKey)
        
        // 도메인 결과 반환
        return AccessKeyCreationResult.from(savedKey, secretKey)
    }
}