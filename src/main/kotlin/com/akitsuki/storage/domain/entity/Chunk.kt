package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(name = "chunks")
data class Chunk(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var size: Long,

    @Column(name = "storage_path", nullable = false)
    var storagePath: String,

    var md5: String? = null,

    var sha256: String? = null,

    @Column(name = "sequence_index", nullable = false)
    var sequenceIndex: Int,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant? = null
)
