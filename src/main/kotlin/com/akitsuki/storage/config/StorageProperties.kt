package com.akitsuki.storage.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "storage.access-key")
data class AccessKeyProperties(
    var prefix: String = "AKIA",
    var keyLength: Int = 16,
    var secretLength: Int = 40,
    var allowedChars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
    var secretChars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
)