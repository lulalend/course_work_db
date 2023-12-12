package com.arslanka.blablacar2.repositories

import com.arslanka.blablacar2.models.Sex
import com.arslanka.blablacar2.models.UserInfo
import org.jooq.DSLContext
import org.jooq.sources.tables.records.UserAccountRecord
import org.jooq.sources.tables.references.USER_ACCOUNT
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val dslContext: DSLContext,
) {

    fun findUserByLogin(login: String): UserInfo? {
        return dslContext.selectFrom(USER_ACCOUNT).where(USER_ACCOUNT.EMAIL.eq(login)).fetchOne()
            ?.toUserInfo()
    }

    private fun UserAccountRecord.toUserInfo(): UserInfo {
        return UserInfo(
            firstName = this.firstName!!,
            lastName = this.lastName!!,
            birthDate = this.birthDate!!,
            email = this.email!!,
            phoneNumber = this.phoneNumber!!,
            password = this.password!!,
            sex = this.sex?.let { Sex.valueOf(it.name) }!!,
        )
    }
}