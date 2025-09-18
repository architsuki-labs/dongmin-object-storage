package com.akitsuki.storage.application.facade

import com.akitsuki.storage.application.mapper.AccessKeyMapper
import com.akitsuki.storage.domain.auth.service.AccessKeyService
import com.akitsuki.storage.interfaces.auth.dto.AccessKeyCreateRequest
import com.akitsuki.storage.interfaces.auth.dto.AccessKeyResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
class AuthFacade(
    private val accessKeyService: AccessKeyService,
    private val accessKeyMapper: AccessKeyMapper
) {
    @Transactional
    fun createAccessKey(userId: UUID, request: AccessKeyCreateRequest): AccessKeyResponse {
        val command = accessKeyMapper.toCommand(userId, request)
        val result = accessKeyService.createAccessKey(command)
        return accessKeyMapper.toResponse(result)
    }
}