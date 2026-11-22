package com.hotelio.bookingservice.service

import com.example.grpc.proto.ReviewServiceGrpc
import com.example.grpc.proto.hotelIdRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReviewService(
    private val reviewServiceStub: ReviewServiceGrpc.ReviewServiceBlockingStub,
) {
    private val log = LoggerFactory.getLogger(ReviewService::class.java)

    fun isTrustedHotel(hotelId: String): Boolean {
        val request = hotelIdRequest { this.hotelId = hotelId }
        return try {
            reviewServiceStub.isTrustedHotel(request).value
        } catch (e: StatusRuntimeException) {
            when (e.status.code) {
                Status.Code.NOT_FOUND -> {
                    log.warn("Hotel {} not found in monolith via gRPC (isTrustedHotel)", hotelId)
                    false
                }
                else -> {
                    log.error("gRPC error while checking trusted status for hotel {}: {}", hotelId, e.status, e)
                    throw IllegalStateException("Failed to check trusted status for hotel $hotelId", e)
                }
            }
        }
    }
}