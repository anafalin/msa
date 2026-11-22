package com.hotelio.monolith.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public final class GrpcResponseHelper {

    private GrpcResponseHelper() {}

    public static <T> void createResponseOk(StreamObserver<T> observer, T value) {
        observer.onNext(value);
        observer.onCompleted();
    }

    public static <T> void createResponseInvalidArgument(StreamObserver<T> observer, String description) {
        observer.onError(
            Status.INVALID_ARGUMENT
                .withDescription(description)
                .asRuntimeException()
        );
    }

    public static <T> void createResponseNotFound(StreamObserver<T> observer, String description) {
        observer.onError(
            Status.NOT_FOUND
                .withDescription(description)
                .asRuntimeException()
        );
    }

    public static <T> void createResponseInternalError(
        StreamObserver<T> observer, String description, Throwable cause) {
        observer.onError(
            Status.INTERNAL
                .withDescription(description)
                .withCause(cause)
                .asRuntimeException()
        );
    }
}