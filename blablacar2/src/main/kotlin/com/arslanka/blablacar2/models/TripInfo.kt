package com.arslanka.blablacar2.models

import java.time.LocalDate

data class TripInfo(
    val departureLocation: String,
    val arrivalLocation: String,
    val availableSeats: Int,
    val date: LocalDate,
    val email: String,
    val description: String?,
    val tripStatus: TripStatus,
)