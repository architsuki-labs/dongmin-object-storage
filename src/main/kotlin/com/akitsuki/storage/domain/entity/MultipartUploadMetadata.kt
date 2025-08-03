package com.akitsuki.storage.domain.entity

import jakarta.persistence.*

@Embeddable
data class MultipartUploadMetadataId(
    @Column(name = "multipart_upload_id")
    var multipartUploadId: Long = 0,

    @Column(name = "meta_key")
    var metaKey: String = ""
) : java.io.Serializable

@Entity
@Table(name = "multipart_upload_metadata")
data class MultipartUploadMetadata(
    @EmbeddedId
    val id: MultipartUploadMetadataId = MultipartUploadMetadataId(),

    @Column(name = "meta_value")
    var metaValue: String? = null
)