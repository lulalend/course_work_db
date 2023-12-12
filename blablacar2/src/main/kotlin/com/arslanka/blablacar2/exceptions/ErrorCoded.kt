package com.arslanka.blablacar2.exceptions

import org.springframework.http.HttpStatus

interface ErrorCoded {
    val code: String
    val status: HttpStatus
}