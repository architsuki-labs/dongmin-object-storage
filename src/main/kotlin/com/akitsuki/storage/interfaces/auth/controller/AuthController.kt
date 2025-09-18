package com.akitsuki.storage.interfaces.auth.controller

import com.akitsuki.storage.application.facade.AuthFacade
import com.akitsuki.storage.interfaces.auth.dto.AccessKeyCreateRequest
import com.akitsuki.storage.interfaces.auth.dto.AccessKeyResponse
import com.akitsuki.storage.interfaces.common.CommonRes
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 관련 API")
class AuthController(
    private val authFacade: AuthFacade
) {
    
    @PostMapping("/users/{userId}/access-keys")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "액세스 키 발급", description = "사용자에게 새로운 액세스 키를 발급합니다")
    fun createAccessKey(
        @PathVariable userId: UUID,
        @RequestBody request: AccessKeyCreateRequest
    ): CommonRes<AccessKeyResponse> {
        val response = authFacade.createAccessKey(userId, request)
        return CommonRes.success(response)
    }
}