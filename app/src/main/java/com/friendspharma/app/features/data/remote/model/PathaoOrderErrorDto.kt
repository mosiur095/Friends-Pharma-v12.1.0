package com.friendspharma.app.features.data.remote.model

data class PathaoOrderErrorDto(
    val code: Int? = null,
    val errors: PathaoOrderErrorsDtoErrors? = null,
    val message: String? = null,
    val type: String? = null
)