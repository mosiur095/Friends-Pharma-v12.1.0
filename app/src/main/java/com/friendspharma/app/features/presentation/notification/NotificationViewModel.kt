package com.friendspharma.app.features.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendspharma.app.features.data.remote.model.NotificationDto
import com.friendspharma.app.features.domain.use_case.DeleteNotificationUseCase
import com.friendspharma.app.features.domain.use_case.GetNotificationsUseCase
import com.friendspharma.app.features.domain.use_case.MarkNotificationReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markRead: MarkNotificationReadUseCase,
    private val deleteNotification: DeleteNotificationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        getNotifications.invoke()
            .onEach { dtos ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        notifications = dtos.map { d -> d.toUi() },
                        unreadCount = dtos.count { d -> !d.isRead },
                    )
                }
            }
            .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: NotificationEvent) {
        when (event) {
            NotificationEvent.Refresh -> _state.update { it.copy(error = null) }
            NotificationEvent.MarkAllAsRead -> viewModelScope.launch { markRead.markAll() }
            NotificationEvent.ClearAll -> viewModelScope.launch {
                state.value.notifications.forEach { deleteNotification.invoke(it.id) }
            }
            is NotificationEvent.MarkAsRead -> viewModelScope.launch { markRead.invoke(event.id) }
            is NotificationEvent.DeleteNotification -> viewModelScope.launch {
                deleteNotification.invoke(event.notification.id)
            }
            is NotificationEvent.NotificationClicked -> viewModelScope.launch {
                if (!event.notification.isRead) markRead.invoke(event.notification.id)
            }
        }
    }
}

private fun NotificationDto.toUi() = NotificationUi(
    id        = id,
    title     = title.orEmpty(),
    body      = body.orEmpty(),
    type      = NotificationType.from(type),
    isRead    = isRead,
    timestamp = timestamp,
    route     = route,
    productId = productId,
    imageUrl  = imageUrl,
)