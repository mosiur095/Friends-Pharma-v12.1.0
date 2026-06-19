package com.friendspharma.app.features.presentation.return_list

import com.friendspharma.app.features.data.remote.model.ReturnList
import com.friendspharma.app.features.data.remote.model.ReturnListDto
import com.friendspharma.app.features.data.remote.model.ReturnListDtoData

data class ReturnListState(
    val isLoading: Boolean = true,
    val returnList: List<ReturnList> = listOf(),
    val currentItem: ReturnList = ReturnList()
)
