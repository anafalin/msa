package com.hotelio.bookingservice.entity

import java.time.LocalDate

data class PromoCode(
    val code: String,
    val discountPercent: String,
    val vipOnly: Boolean,
    val active: Boolean,
)