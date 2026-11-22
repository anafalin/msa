package com.hotelio.bookingservice.service

import com.example.grpc.proto.PromoCodeServiceGrpc
import com.example.grpc.proto.promoValidateRequest
import com.hotelio.bookingservice.entity.PromoCode
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PromoCodeService(
    private val promoCodeServiceStub: PromoCodeServiceGrpc.PromoCodeServiceBlockingStub,
) {
    private val log = LoggerFactory.getLogger(PromoCodeService::class.java)

    fun validate(promoCode: String, userId: String): PromoCode? {
        val request = promoValidateRequest {
            this.userId = userId
            this.code = promoCode
        }

        return try {
            val response = promoCodeServiceStub.validate(request)
            PromoCode(
                code = response.code,
                discountPercent = response.discountPercent.toString(),
                vipOnly = response.vipOnly,
                active = response.active,
            )
        } catch (e: StatusRuntimeException) {
            when (e.status.code) {
                Status.Code.NOT_FOUND -> {
                    log.warn("Promo code {} not found for user {} via gRPC", promoCode, userId)
                    null
                }
                else -> {
                    log.error(
                        "gRPC error while validating promo code {} for user {}: {}",
                        promoCode, userId, e.status, e
                    )
                    throw IllegalStateException("Failed to validate promo code via monolith", e)
                }
            }
        }
    }
}