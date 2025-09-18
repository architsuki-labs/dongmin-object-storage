package com.akitsuki.storage.application.mapper

import com.akitsuki.storage.domain.auth.model.AccessKeyCreationCommand
import com.akitsuki.storage.domain.auth.model.AccessKeyCreationResult
import com.akitsuki.storage.interfaces.auth.dto.AccessKeyCreateRequest
import com.akitsuki.storage.interfaces.auth.dto.AccessKeyResponse
import org.springframework.stereotype.Component
import java.util.*

@Component
class AccessKeyMapper {
    
    fun toCommand(userId: UUID, request: AccessKeyCreateRequest): AccessKeyCreationCommand {
        return AccessKeyCreationCommand(
            userId = userId,
            description = request.description,
            expiresInDays = request.expiresInDays
        )
    }
    
    fun toResponse(result: AccessKeyCreationResult): AccessKeyResponse {
        return AccessKeyResponse(
            accessKeyId = result.accessKeyId,
            secretAccessKey = result.secretKey,
            status = result.status.name,
            createdAt = result.createdAt,
            expiresAt = result.expiresAt
        )
    }
}