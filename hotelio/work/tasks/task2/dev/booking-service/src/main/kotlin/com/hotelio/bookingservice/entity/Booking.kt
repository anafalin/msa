package com.hotelio.bookingservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant

@Entity
class Booking(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId:  String,
    val hotelId:  String,

    val promoCode:  String? = null,
    val discountPercent:  Double,

    @Column(nullable = false)
    val price:  Double,
    val createdAt: Instant = Instant.now()
)