package com.hotelio.monolith.grpc;

import com.example.grpc.proto.BooleanResponse;
import com.example.grpc.proto.HotelIdRequest;
import com.example.grpc.proto.HotelResponse;
import com.example.grpc.proto.HotelServiceGrpc;
import com.hotelio.monolith.entity.Hotel;
import com.hotelio.monolith.service.HotelService;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInternalError;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInvalidArgument;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseNotFound;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseOk;

@GrpcService
public class HotelGrpcService extends HotelServiceGrpc.HotelServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(HotelGrpcService.class);

    private final HotelService service;

    public HotelGrpcService(HotelService service) {
        this.service = service;
    }

    @Override
    public void getHotelById(HotelIdRequest request, StreamObserver<HotelResponse> responseObserver) {
        if (request.getHotelId().isBlank()) {
            createResponseInvalidArgument(responseObserver, "hotelId: must not be blank");
            return;
        }

        try {
            Optional<Hotel> optionalHotel = service.getHotelById(request.getHotelId());
            if (optionalHotel.isEmpty()) {
                createResponseNotFound(responseObserver, "Hotel not found with id: " + request.getHotelId());
                return;
            }

            Hotel hotel = optionalHotel.get();
            HotelResponse response = HotelResponse.newBuilder()
                .setId(hotel.getId())
                .setCity(hotel.getCity())
                .setName(hotel.getDescription())
                .setRating(hotel.getRating())
                .build();

            createResponseOk(responseObserver, response);
        } catch (Exception e) {
            log.error("Failed to get hotel by id {}", request.getHotelId(), e);
            createResponseInternalError(responseObserver, "Internal error while fetching hotel", e);
        }
    }

    @Override
    public void isHotelOperational(HotelIdRequest request, StreamObserver<BooleanResponse> responseObserver) {
        if (request.getHotelId().isBlank()) {
            createResponseInvalidArgument(responseObserver, "hotelId: must not be blank");
            return;
        }

        try {
            boolean isOperational = service.isHotelOperational(request.getHotelId());
            BooleanResponse response = BooleanResponse.newBuilder()
                .setValue(isOperational)
                .build();

            createResponseOk(responseObserver, response);
        } catch (Exception e) {
            log.error("Failed to check operational status for hotel {}", request.getHotelId(), e);
            createResponseInternalError(
                responseObserver,
                "Internal error while checking hotel operational status",
                e
            );
        }
    }

    @Override
    public void isHotelFullyBooked(HotelIdRequest request, StreamObserver<BooleanResponse> responseObserver) {
        if (request.getHotelId().isBlank()) {
            createResponseInvalidArgument(responseObserver, "hotelId: must not be blank");
            return;
        }

        try {
            boolean fullyBooked = service.isHotelFullyBooked(request.getHotelId());
            BooleanResponse response = BooleanResponse.newBuilder()
                .setValue(fullyBooked)
                .build();

            createResponseOk(responseObserver, response);
        } catch (Exception e) {
            log.error("Failed to check booking status for hotel {}", request.getHotelId(), e);
            createResponseInternalError(
                responseObserver,
                "Internal error while checking hotel booking status",
                e
            );
        }
    }
}