package com.hotelio.bookingservice.grpc

import com.hotelio.bookingservice.entity.Booking
import com.hotelio.bookingservice.grpc.GrpcResponseHelper.createResponseInternalError
import com.hotelio.bookingservice.grpc.GrpcResponseHelper.createResponseInvalidArgument
import com.hotelio.bookingservice.grpc.GrpcResponseHelper.createResponseOk
import com.hotelio.bookingservice.service.BookingService
import com.hotelio.proto.booking.BookingListRequest
import com.hotelio.proto.booking.BookingListResponse
import com.hotelio.proto.booking.BookingRequest
import com.hotelio.proto.booking.BookingResponse
import com.hotelio.proto.booking.BookingServiceGrpc
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class BookingGrpcService(
    private val bookingService: BookingService,
) : BookingServiceGrpc.BookingServiceImplBase() {

    private val log = LoggerFactory.getLogger(BookingGrpcService::class.java)

    override fun createBooking(
        request: BookingRequest,
        responseObserver: StreamObserver<BookingResponse>,
    ) {
        if (request.userId.isBlank()) {
            createResponseInvalidArgument(responseObserver, "userId: must not be blank")
            return
        }
        if (request.hotelId.isBlank()) {
            createResponseInvalidArgument(responseObserver, "hotelId: must not be blank")
            return
        }

        try {
            val response = bookingService.createBooking(
                userId = request.userId,
                hotelId = request.hotelId,
                promoCode = request.promoCode,
            ).toBookingResponse()

            createResponseOk(responseObserver, response)
        } catch (e: Exception) {
            log.error(
                "Failed to create booking for user {} and hotel {}",
                request.userId, request.hotelId, e,
            )
            createResponseInternalError(responseObserver, "Internal error while creating booking", e)
        }
    }

    override fun listBookings(
        request: BookingListRequest,
        responseObserver: StreamObserver<BookingListResponse>,
    ) {
        if (request.userId.isBlank()) {
            createResponseInvalidArgument(responseObserver, "userId: must not be blank")
            return
        }

        try {
            val bookings = bookingService.listAll(request.userId).map { it.toBookingResponse() }

            val response = BookingListResponse.newBuilder()
                .addAllBookings(bookings)
                .build()

            createResponseOk(responseObserver, response)
        } catch (e: Exception) {
            log.error("Failed to list bookings for user {}", request.userId, e)
            createResponseInternalError(responseObserver, "Internal error while listing bookings", e)
        }
    }

    private fun Booking.toBookingResponse() = BookingResponse.newBuilder()
        .setId(id.toString())
        .setDiscountPercent(discountPercent)
        .setHotelId(hotelId)
        .setPromoCode(promoCode)
        .setCreatedAt(createdAt.toString())
        .build()
}