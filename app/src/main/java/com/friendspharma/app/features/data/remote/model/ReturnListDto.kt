package com.friendspharma.app.features.data.remote.model

data class ReturnListDto(
    val `data`: List<ReturnListDtoData>? = null,
    val message: String? = null,
    val status: Int? = null
)