package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "buckets")
data class Bucket(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(name = "owner_user_id", nullable = false)
    var ownerUserId: Long,

    @Column(name = "versioning_enabled", nullable = false)
    var versioningEnabled: Boolean = false,

    @Column(name = "storage_class")
    var storageClass: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
)
