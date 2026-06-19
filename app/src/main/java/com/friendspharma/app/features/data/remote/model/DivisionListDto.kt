package com.friendspharma.app.features.data.remote.model

import com.google.gson.annotations.SerializedName

data class DivisionListDto(
    @SerializedName("status")
    val status: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<DivisionData>? = null
)

data class DivisionData(
    @SerializedName("DIVISION_ID")
    val divisionId: Int? = null,
    @SerializedName("DIVISION_NAME")
    val divisionName: String? = null,
    @SerializedName("DIVISION_BN")
    val divisionBn: String? = null,
)
