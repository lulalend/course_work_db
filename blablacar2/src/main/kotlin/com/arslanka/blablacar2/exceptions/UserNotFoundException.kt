package com.arslanka.blablacar2.exceptions

import org.springframework.http.HttpStatus

class UserNotFoundException(email: String) : RuntimeException(
    "User with email=$email wasn't found"
), ErrorCoded {
    override val code = "UserNotFoundException"
    override val status = HttpStatus.NOT_FOUND
}
