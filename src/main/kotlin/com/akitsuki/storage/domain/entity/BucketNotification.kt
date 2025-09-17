package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "bucket_notifications",
    indexes = [
        Index(name = "idx_bucket_notifications_bucket", columnList = "bucket_id")
    ]
)
class BucketNotification(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "bucket_id", nullable = false, columnDefinition = "uuid")
    val bucketId: UUID,

    @Column(name = "event_type", nullable = false, length = 100)
    val eventType: String,

    @Column(name = "destination_arn", length = 500)
    val destinationArn: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filter_rules", columnDefinition = "jsonb")
    val filterRules: Map<String, Any>? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)