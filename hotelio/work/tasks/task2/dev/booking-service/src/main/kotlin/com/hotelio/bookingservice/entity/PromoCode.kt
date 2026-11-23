package com.hotelio.bookingservice.entity

data class PromoCode(
    val code: String,
    val discountPercent: String,
    val vipOnly: Boolean,
    val active: Boolean,
)