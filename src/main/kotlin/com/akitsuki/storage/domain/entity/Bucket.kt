package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "buckets",
    indexes = [
        Index(name = "idx_buckets_name", columnList = "name", unique = true),
        Index(name = "idx_buckets_owner", columnList = "owner_id")
    ]
)
class Bucket(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 63)
    val name: String,

    @Column(name = "owner_id", nullable = false, columnDefinition = "uuid")
    val ownerId: UUID,

    @Column(nullable = false, length = 30)
    val region: String = "us-east-1",

    @Column(name = "versioning_enabled", nullable = false)
    var versioningEnabled: Boolean = false,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "access_policy", columnDefinition = "jsonb")
    var accessPolicy: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cors_rules", columnDefinition = "jsonb")
    var corsRules: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lifecycle_rules", columnDefinition = "jsonb")
    var lifecycleRules: Map<String, Any>? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
}