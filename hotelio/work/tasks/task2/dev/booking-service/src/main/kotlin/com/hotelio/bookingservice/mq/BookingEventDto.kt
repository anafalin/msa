package com.hotelio.bookingservice.mq

import com.hotelio.bookingservice.entity.Booking
import java.time.Instant

class BookingEventDto(
    val id: Long,
    val userId: String,
    val hotelId: String,
    val promoCode: String?,
    val discountPercent: Double,
    val price: Double,
    val createdAt: Instant,
)

fun Booking.toEvent() = BookingEventDto(
    id = id!!,
    userId = userId,
    hotelId = hotelId,
    promoCode = promoCode,
    discountPercent = discountPercent,
    price = price,
    createdAt = createdAt,
)