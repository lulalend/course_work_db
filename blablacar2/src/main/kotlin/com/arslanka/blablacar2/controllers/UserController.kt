package com.arslanka.blablacar2.controllers

import com.arslanka.blablacar2.api.UserApi
import com.arslanka.blablacar2.model.UserInfo
import com.arslanka.blablacar2.model.UserLogin
import com.arslanka.blablacar2.models.Sex
import com.arslanka.blablacar2.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import com.arslanka.blablacar2.models.UserInfo as UserInfoModel

@RestController
@CrossOrigin(origins = ["*"])
class UserController(
    private val userService: UserService,
) : UserApi {

    override fun v1UserInfoGetPost(userLogin: UserLogin): ResponseEntity<UserInfo> {
        return ResponseEntity.ok(
            userService.getUserInfo(userLogin.login, userLogin.password).toUserInfoDto()
        )
    }

    private fun UserInfoModel.toUserInfoDto() =
        UserInfo(
            firstName = this.firstName,
            lastName = this.lastName,
            birthDate = this.birthDate,
            sex = when (this.sex) {
                Sex.MALE -> UserInfo.Sex.mALE
                Sex.FEMALE -> UserInfo.Sex.fEMALE
                Sex.OTHER -> UserInfo.Sex.oTHER
            },
            phoneNumber = this.phoneNumber,
            email = this.email,
        )
}