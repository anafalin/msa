package com.hotelio.bookinghistoryservice.repository

import com.hotelio.bookinghistoryservice.entity.Booking
import org.springframework.data.jpa.repository.JpaRepository

interface BookingRepository: JpaRepository<Booking, Long>