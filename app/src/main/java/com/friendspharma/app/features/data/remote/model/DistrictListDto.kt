package com.friendspharma.app.features.data.remote.model

import com.google.gson.annotations.SerializedName

data class DistrictListDto(
    @SerializedName("status")
    val status: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<DistrictData>? = null
)

data class DistrictData(
    @SerializedName("DISTRICT_ID")
    val districtId: Int? = null,
    @SerializedName("DISTRICT_NAME")
    val districtName: String? = null,
    @SerializedName("DISTRICT_BN")
    val districtBn: String? = null,
)
