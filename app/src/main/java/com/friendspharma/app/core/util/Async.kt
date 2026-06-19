package com.friendspharma.app.core.util

sealed class Async<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Async<T>(data)
    class Error<T>(message: String?, data: T? = null) : Async<T>(data, message)
    class Loading<T>(data: T? = null) : Async<T>(data)
}
