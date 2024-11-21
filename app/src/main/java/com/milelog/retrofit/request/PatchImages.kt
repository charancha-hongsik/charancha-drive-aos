package com.milelog.retrofit.request

data class PatchImages (
    val images:Images
) : java.io.Serializable

data class Images(
    val delete: List<DeleteImage>
)

data class DeleteImage(
    val id: String
)