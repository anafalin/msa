package com.hotelio.bookingservice.service

import com.example.grpc.proto.HotelServiceGrpc
import com.example.grpc.proto.hotelIdRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HotelService(
    private val hotelServiceStub: HotelServiceGrpc.HotelServiceBlockingStub,
) {
    private val log = LoggerFactory.getLogger(HotelService::class.java)

    fun isHotelOperational(hotelId: String): Boolean {
        val request = hotelIdRequest { this.hotelId = hotelId }
        return try {
            hotelServiceStub.isHotelOperational(request).value
        } catch (e: StatusRuntimeException) {
            handleBooleanCall("isHotelOperational", hotelId, e)
        }
    }

    fun isHotelFullyBooked(hotelId: String): Boolean {
        val request = hotelIdRequest { this.hotelId = hotelId }
        return try {
            hotelServiceStub.isHotelFullyBooked(request).value
        } catch (e: StatusRuntimeException) {
            handleBooleanCall("isHotelFullyBooked", hotelId, e)
        }
    }

    private fun handleBooleanCall(operation: String, hotelId: String, e: StatusRuntimeException): Boolean {
        return when (e.status.code) {
            Status.Code.NOT_FOUND -> {
                log.warn("Hotel {} not found in monolith via gRPC ({})", hotelId, operation)
                false
            }

            else -> {
                log.error("gRPC error while calling {} for hotel {}: {}", operation, hotelId, e.status, e)
                throw IllegalStateException("Failed to call $operation for hotel $hotelId", e)
            }
        }
    }
}