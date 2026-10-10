package com.movieapp.core.common

/** A generic class that holds a value or an error. */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()

    data class Error(val exception: Throwable) : Result<Nothing>()

    data object Loading : Result<Nothing>()
}

/** Returns the data if this is a Success, null otherwise. */
fun <T> Result<T>.getDataOrNull(): T? =
    when (this) {
        is Result.Success -> data
        else -> null
    }

/** Returns the exception if this is an Error, null otherwise. */
fun <T> Result<T>.getExceptionOrNull(): Throwable? =
    when (this) {
        is Result.Error -> exception
        else -> null
    }

/** Returns true if this is a Success result. */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/** Returns true if this is an Error result. */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/** Returns true if this is a Loading result. */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/** Maps the data if this is a Success, otherwise returns the same result type. */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception)
        is Result.Loading -> Result.Loading
    }

/**
 * Maps the data if this is a Success, otherwise returns the same result type. The transform
 * function can return a Result.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> Result.Error(exception)
        is Result.Loading -> Result.Loading
    }

/** Executes the given action if this is a Success. */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> = apply {
    if (this is Result.Success) action(data)
}

/** Executes the given action if this is an Error. */
inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> = apply {
    if (this is Result.Error) action(exception)
}

/** Executes the given action if this is Loading. */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> = apply {
    if (this is Result.Loading) action()
}

/** Returns the data if this is a Success, otherwise returns the default value. */
fun <T> Result<T>.getOrDefault(defaultValue: T): T =
    when (this) {
        is Result.Success -> data
        else -> defaultValue
    }

/** Returns the data if this is a Success, otherwise returns the result of the default function. */
inline fun <T> Result<T>.getOrElse(defaultValue: () -> T): T =
    when (this) {
        is Result.Success -> data
        else -> defaultValue()
    }
