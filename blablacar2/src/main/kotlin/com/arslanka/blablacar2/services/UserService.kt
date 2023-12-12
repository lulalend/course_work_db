package com.arslanka.blablacar2.services

import com.arslanka.blablacar2.exceptions.AuthenticationException
import com.arslanka.blablacar2.exceptions.UserNotFoundException
import com.arslanka.blablacar2.models.UserInfo
import com.arslanka.blablacar2.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun getUserInfo(login: String, password: String): UserInfo {
        val userInfo = userRepository.findUserByLogin(login) ?: throw UserNotFoundException(email = login)
        if (userInfo.password != password) throw AuthenticationException(userInfo.email)
        return userInfo
    }
}