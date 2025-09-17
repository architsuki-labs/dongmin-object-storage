package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "access_keys",
    indexes = [
        Index(name = "idx_access_keys_user", columnList = "user_id"),
        Index(name = "idx_access_keys_active", columnList = "access_key_id", unique = true)
    ]
)
class AccessKey(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @Column(name = "access_key_id", nullable = false, unique = true, length = 20)
    val accessKeyId: String,

    @Column(name = "secret_key_hash", nullable = false)
    val secretKeyHash: String,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "last_used")
    var lastUsed: Instant? = null,

    @Column(name = "expires_at")
    val expiresAt: Instant? = null
)