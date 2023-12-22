package com.arslanka.blablacar2.models

import java.time.LocalDate

data class UserInfo(
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate,
    val sex: Sex,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val isDriver: Boolean,
)

enum class Sex {
    MALE,
    FEMALE,
    OTHER,
}