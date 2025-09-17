package com.akitsuki.storage.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "object_locks",
    indexes = [
        Index(name = "idx_object_locks_version", columnList = "version_id"),
        Index(name = "idx_object_locks_retain", columnList = "retain_until")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_object_locks_version",
            columnNames = ["version_id"]
        )
    ]
)
class ObjectLock(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    val id: UUID? = null,

    @Column(name = "version_id", nullable = false, columnDefinition = "uuid")
    val versionId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_mode", length = 20)
    val lockMode: LockMode? = null,

    @Column(name = "retain_until")
    val retainUntil: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "legal_hold_status", length = 10)
    var legalHoldStatus: LegalHoldStatus = LegalHoldStatus.OFF,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    enum class LockMode {
        COMPLIANCE,
        GOVERNANCE
    }

    enum class LegalHoldStatus {
        ON,
        OFF
    }
}