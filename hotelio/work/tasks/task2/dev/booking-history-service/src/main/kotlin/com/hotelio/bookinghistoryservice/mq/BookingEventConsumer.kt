package com.hotelio.bookinghistoryservice.mq

import com.hotelio.bookinghistoryservice.repository.BookingRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class BookingEventConsumer(
    private val bookingRepository: BookingRepository,
) {

    private val log = LoggerFactory.getLogger(BookingEventConsumer::class.java)

    @KafkaListener(
        topics = ["booking-events"],
        groupId = "booking-service",
    )
    fun onBookingEvent(event: BookingEventDto) {
        log.info("Received booking event: {}", event)
        bookingRepository.save(event.toEntity())
    }
}