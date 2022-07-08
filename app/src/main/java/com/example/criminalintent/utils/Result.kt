package com.example.criminalintent.utils

sealed class Result<out Error, out Value> {
    data class Failure<out Error>(val error: Error) : Result<Error, Nothing>()
    data class Success<out Value>(val value: Value) : Result<Nothing, Value>()
}

inline fun <Error, Value> Result<Error, Value>.onSuccess(
    action: (value: Value) -> Unit
): Result<Error, Value> {
    if (this is Result.Success) {
        action(value)
    }
    return this
}

inline fun <Error, Value> Result<Error, Value>.onFailure(
    action: (error: Error) -> Unit
): Result<Error, Value> {
    if (this is Result.Failure) {
        action(error)
    }
    return this
}
