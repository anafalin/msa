package com.hotelio.bookingservice.service

import com.hotelio.bookingservice.entity.Booking
import com.hotelio.bookingservice.entity.PromoCode
import com.hotelio.bookingservice.mq.toEvent
import com.hotelio.bookingservice.repository.BookingRepository
import java.util.Optional
import java.util.function.Function
import java.util.function.Supplier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BookingService(
    private final val bookingRepository: BookingRepository,
    private final val promoCodeService: PromoCodeService,
    private final val reviewService: ReviewService,
    private final val userService: AppUserService,
    private final val hotelService: HotelService,
    private final val sendBookingEvent: SendBookingEvent,
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(BookingService::class.java)
    }

    fun listAll(userId: String?): List<Booking> {
        log.info("Listing all bookings")
        return userId?.let {
            bookingRepository.findByUserId(it)
        } ?: bookingRepository.findAll()
    }

    fun createBooking(userId: String, hotelId: String, promoCode: String): Booking {
        log.info("Creating booking: userId={}, hotelId={}, promoCode={}", userId, hotelId, promoCode)

        validateUser(userId)
        validateHotel(hotelId)

        val basePrice = resolveBasePrice(userId)
        val discount = resolvePromoDiscount(promoCode, userId)

        val finalPrice = basePrice - discount

        log.info(
            "Final price calculated: base={}, discount={}, final={}",
            basePrice,
            discount,
            finalPrice
        )

        val booking = Booking(
            userId = userId,
            hotelId = hotelId,
            promoCode = promoCode,
            discountPercent = discount,
            price = finalPrice,
        )
        val savedBooking = bookingRepository.save(booking)
        sendBookingEvent(savedBooking.toEvent())
        return savedBooking
    }

    private fun validateUser(userId: String) {
        if (!userService.isUserActive(userId)) {
            log.warn("User {} is inactive", userId)
            throw IllegalArgumentException("User is inactive")
        }
        if (userService.isUserBlacklisted(userId)) {
            log.warn("User {} is blacklisted", userId)
            throw IllegalArgumentException("User is blacklisted")
        }
    }

    private fun validateHotel(hotelId: String) {
        if (!hotelService.isHotelOperational(hotelId)) {
            log.warn("Hotel {} is not operational", hotelId)
            throw IllegalArgumentException("Hotel is not operational")
        }
        if (!reviewService.isTrustedHotel(hotelId)) {
            log.warn("Hotel {} is not trusted", hotelId)
            throw IllegalArgumentException("Hotel is not trusted based on reviews")
        }
        if (hotelService.isHotelFullyBooked(hotelId)) {
            log.warn("Hotel {} is fully booked", hotelId)
            throw IllegalArgumentException("Hotel is fully booked")
        }
    }

    private fun resolveBasePrice(userId: String): Double {
        val statusOpt: Optional<String> = userService.getUserStatus(userId)
        return statusOpt.map(Function { status: String? ->
            val isVip = status.equals("VIP", ignoreCase = true)
            log.debug("User {} has status '{}', base price is {}", userId, status, if (isVip) 80.0 else 100.0)
            if (isVip) 80.0 else 100.0
        }).orElseGet(Supplier {
            log.debug("User {} has unknown status, default base price 100.0", userId)
            100.0
        })
    }

    private fun resolvePromoDiscount(promoCode: String, userId: String): Double {
        if (promoCode.isEmpty()) return 0.0

        val promo: PromoCode? = promoCodeService.validate(promoCode, userId)
        if (promo == null) {
            log.info("Promo code '{}' is invalid or not applicable for user {}", promoCode, userId)
            return 0.0
        }

        log.debug("Promo code '{}' applied with discount {}", promoCode, promo.discountPercent)
        return promo.discountPercent.toDouble()
    }
}