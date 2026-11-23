package com.hotelio.bookinghistoryservice.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
data class Booking(
    @Id
    val id: Long,
    val userId:  String,
    val hotelId:  String,
    val promoCode:  String? = null,
    val discountPercent:  Double,
    val price:  Double,
    val createdAt: Instant,
)