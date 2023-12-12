package com.arslanka.blablacar2.exceptions

import org.springframework.http.HttpStatus

class AuthenticationException(email: String) : RuntimeException(
    "User's password for email=$email is incorrect",
), ErrorCoded {
    override val code = "AuthenticationException"
    override val status = HttpStatus.UNAUTHORIZED
}