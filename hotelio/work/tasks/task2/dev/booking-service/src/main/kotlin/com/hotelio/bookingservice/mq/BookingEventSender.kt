package com.hotelio.bookingservice.mq

import com.hotelio.bookingservice.service.BookingService.Companion.log
import com.hotelio.bookingservice.service.SendBookingEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class BookingEventSender(
    private val kafkaTemplate: KafkaTemplate<String, BookingEventDto>,
) : SendBookingEvent {

    companion object {
        const val TOPIC = "booking-events"
    }

    override fun invoke(event: BookingEventDto) {
        kafkaTemplate.send(TOPIC, event.id.toString(), event)
            .whenComplete { result, ex ->
                if (ex != null) {
                    log.error("Failed to send: ${ex.message}")
                } else {
                    log.info(
                        "Sent to ${result.recordMetadata.topic()}:" +
                                "${result.recordMetadata.partition()}" +
                                "@${result.recordMetadata.offset()}"
                    )
                }
            }
    }

}