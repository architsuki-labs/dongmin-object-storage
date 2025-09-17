package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "chunks",
    indexes = [
        Index(name = "idx_chunks_version_seq", columnList = "version_id, sequence")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_chunks_version_sequence",
            columnNames = ["version_id", "sequence"]
        )
    ]
)
class Chunk(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "version_id", nullable = false, columnDefinition = "uuid")
    val versionId: UUID,

    @Column(nullable = false)
    val sequence: Int,

    @Column(name = "storage_path", nullable = false, length = 500)
    val storagePath: String,

    @Column(nullable = false)
    val size: Long,

    @Column(name = "md5_hash", length = 32)
    val md5Hash: String? = null,

    @Column(name = "sha256_hash", length = 64)
    val sha256Hash: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)