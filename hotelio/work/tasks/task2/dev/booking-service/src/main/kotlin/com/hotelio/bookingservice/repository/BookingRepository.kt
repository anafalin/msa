package com.hotelio.bookingservice.repository

import com.hotelio.bookingservice.entity.Booking
import org.springframework.data.jpa.repository.JpaRepository

interface BookingRepository: JpaRepository<Booking, Long> {
    fun findByUserId(userId: String): List<Booking>
}