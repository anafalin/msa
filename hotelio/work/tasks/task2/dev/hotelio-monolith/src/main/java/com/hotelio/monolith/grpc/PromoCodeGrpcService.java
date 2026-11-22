package com.hotelio.monolith.grpc;

import com.example.grpc.proto.PromoCodeServiceGrpc;
import com.example.grpc.proto.PromoResponse;
import com.example.grpc.proto.PromoValidateRequest;
import com.hotelio.monolith.entity.PromoCode;
import com.hotelio.monolith.service.PromoCodeService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInternalError;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseInvalidArgument;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseNotFound;
import static com.hotelio.monolith.grpc.GrpcResponseHelper.createResponseOk;

@GrpcService
public class PromoCodeGrpcService extends PromoCodeServiceGrpc.PromoCodeServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PromoCodeGrpcService.class);

    private final PromoCodeService service;

    public PromoCodeGrpcService(PromoCodeService service) {
        this.service = service;
    }

    @Override
    public void validate(PromoValidateRequest request, StreamObserver<PromoResponse> responseObserver) {
        if (request.getCode().isBlank()) {
            createResponseInvalidArgument(responseObserver, "promo code must not be blank");
            return;
        }
        if (request.getUserId().isBlank()) {
            createResponseInvalidArgument(responseObserver, "userId: must not be blank");
            return;
        }

        try {
            PromoCode promoCode = service.validate(request.getCode(), request.getUserId());

            if (promoCode == null) {
                createResponseNotFound(
                    responseObserver,
                    "PromoCode not found with userId: " + request.getUserId() + " and promocode: " + request.getCode()
                );
                return;
            }

            PromoResponse response = PromoResponse.newBuilder()
                .setCode(promoCode.getCode())
                .setDiscountPercent(promoCode.getDiscount())
                .setVipOnly(promoCode.isVipOnly())
                .setActive(true)
                .build();

            createResponseOk(responseObserver, response);
        } catch (Exception e) {
            log.error(
                "Failed to validate promo code {} for user {}",
                request.getCode(), request.getUserId(), e
            );
            createResponseInternalError(responseObserver, "Internal error while validating promo code", e);
        }
    }
}