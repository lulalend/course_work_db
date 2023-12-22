package com.arslanka.blablacar2.services

import com.arslanka.blablacar2.exceptions.AuthenticationException
import com.arslanka.blablacar2.exceptions.UserNotFoundException
import com.arslanka.blablacar2.models.UserInfo
import com.arslanka.blablacar2.repositories.UserRepository
import com.arslanka.blablacar2.utils.Logging
import com.arslanka.blablacar2.utils.logger
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    private companion object : Logging {
        val log = logger()
    }

    fun getUserInfo(login: String, password: String): UserInfo {
        log.info("getUserInfo with login=$login")
        val userInfo = userRepository.findUserByLogin(login)

        if (userInfo == null) {
            log.info("User with login=$login wasn't found")
            throw UserNotFoundException(email = login)
        }

        if (userInfo.password != password) {
            log.info("Authentication for user with  login=$login was failed")
            throw AuthenticationException(userInfo.email)
        }

        return userInfo
    }
}