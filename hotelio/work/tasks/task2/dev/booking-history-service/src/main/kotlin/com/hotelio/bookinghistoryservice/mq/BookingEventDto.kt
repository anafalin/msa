package com.hotelio.bookinghistoryservice.mq

import com.hotelio.bookinghistoryservice.entity.Booking
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

fun BookingEventDto.toEntity() = Booking(
    id = id,
    userId = userId,
    hotelId = hotelId,
    promoCode = promoCode,
    discountPercent = discountPercent,
    price = price,
    createdAt = createdAt,
)