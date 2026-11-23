package com.hotelio.bookingservice.service

import com.hotelio.bookingservice.mq.BookingEventDto

interface SendBookingEvent {
    operator fun invoke(event: BookingEventDto)
}