package com.arslanka.blablacar2.exceptions

import com.arslanka.blablacar2.model.Error
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(value = [Exception::class])
    fun onAnyException(ex: Exception, request: HttpServletRequest): ResponseEntity<Error> {
        val response = if (ex is ErrorCoded) {
            ResponseEntity(
                Error(
                    code = ex.code,
                    message = ex.code
                ),
                ex.status,
            )
        } else {
            ResponseEntity(
                Error(
                    code = "InternalError",
                    message = ex.message ?: "InternalError"
                ),
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }

        return response
    }
}