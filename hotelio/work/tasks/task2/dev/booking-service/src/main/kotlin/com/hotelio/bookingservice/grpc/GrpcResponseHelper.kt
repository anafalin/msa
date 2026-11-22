package com.hotelio.bookingservice.grpc

import io.grpc.Status
import io.grpc.stub.StreamObserver

object GrpcResponseHelper {

    fun <T> createResponseOk(observer: StreamObserver<T>, value: T) {
        observer.onNext(value)
        observer.onCompleted()
    }

    fun <T> createResponseInvalidArgument(observer: StreamObserver<T>, description: String) {
        observer.onError(
            Status.INVALID_ARGUMENT
                .withDescription(description)
                .asRuntimeException()
        )
    }

    fun <T> createResponseNotFound(observer: StreamObserver<T>, description: String) {
        observer.onError(
            Status.NOT_FOUND
                .withDescription(description)
                .asRuntimeException()
        )
    }

    fun <T> createResponseInternalError(
        observer: StreamObserver<T>,
        description: String,
        cause: Throwable,
    ) {
        observer.onError(
            Status.INTERNAL
                .withDescription(description)
                .withCause(cause)
                .asRuntimeException()
        )
    }
}