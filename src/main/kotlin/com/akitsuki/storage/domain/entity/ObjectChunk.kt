package com.akitsuki.storage.domain.entity

import jakarta.persistence.*

@Embeddable
data class ObjectChunkId(
    @Column(name = "object_id")
    var objectId: Long = 0,

    @Column(name = "chunk_id")
    var chunkId: Long = 0
) : java.io.Serializable

@Entity
@Table(name = "object_chunk")
data class ObjectChunk(
    @EmbeddedId
    val id: ObjectChunkId = ObjectChunkId(),

    @Column(name = "sequence_index", nullable = false)
    var sequenceIndex: Int
)