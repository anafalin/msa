package com.hotelio.bookingservice.controller

import com.hotelio.bookingservice.entity.Booking
import com.hotelio.bookingservice.service.BookingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bookings")
class BookingController(
    final val bookingService: BookingService,
) {
    // GET /api/bookings?userId=123
    @GetMapping
    fun listBookings(@RequestParam(required = false) userId: String): List<Booking> {
        return bookingService.listAll(userId)
    }

    // POST /api/bookings
    @PostMapping
    fun createBooking(
        @RequestParam userId: String,
        @RequestParam hotelId: String,
        @RequestParam(required = false, defaultValue = "") promoCode: String,
    ): ResponseEntity<Booking> {
        val booking: Booking = bookingService.createBooking(userId, hotelId, promoCode)
        return ResponseEntity.ok(booking)
    }

}