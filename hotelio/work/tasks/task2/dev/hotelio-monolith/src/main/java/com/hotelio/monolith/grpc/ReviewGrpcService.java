package com.hotelio.monolith.grpc;

import com.example.grpc.proto.BooleanResponse;
import com.example.grpc.proto.HotelIdRequest;
import com.example.grpc.proto.ReviewServiceGrpc;
import com.hotelio.monolith.service.ReviewService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInternalError;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInvalidArgument;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseOk;

@GrpcService
public class ReviewGrpcService extends ReviewServiceGrpc.ReviewServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(ReviewGrpcService.class);

    private final ReviewService service;

    public ReviewGrpcService(ReviewService service) {
        this.service = service;
    }

    @Override
    public void isTrustedHotel(HotelIdRequest request, StreamObserver<BooleanResponse> responseObserver) {
        if (request.getHotelId().isBlank()) {
            createResponseInvalidArgument(responseObserver, "hotelId: must not be blank");
            return;
        }

        try {
            boolean isTrustedHotel = service.isTrustedHotel(request.getHotelId());
            BooleanResponse response = BooleanResponse.newBuilder()
                .setValue(isTrustedHotel)
                .build();

            createResponseOk(responseObserver, response);
        } catch (Exception e) {
            log.error("Failed to check trusted status for hotel {}", request.getHotelId(), e);
            createResponseInternalError(
                responseObserver,
                "Internal error while checking hotel trusted status",
                e
            );
        }
    }
}