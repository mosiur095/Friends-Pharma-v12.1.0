package com.friendspharma.app.features.data.remote.model

import com.google.gson.annotations.SerializedName

data class ThanaListDto(
    @SerializedName("status")
    val status: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<ThanaData>? = null
)

data class ThanaData(
    @SerializedName("THANA_ID")
    val thanaId: Int? = null,
    @SerializedName("THANA_NAME")
    val thanaName: String? = null,
    @SerializedName("THANA_BN")
    val thanaBn: String? = null,
)
