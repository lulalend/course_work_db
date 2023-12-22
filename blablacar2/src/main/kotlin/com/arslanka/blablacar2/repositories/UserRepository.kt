package com.arslanka.blablacar2.repositories

import com.arslanka.blablacar2.models.Sex
import com.arslanka.blablacar2.models.UserInfo
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.condition
import org.jooq.sources.tables.references.DRIVER
import org.jooq.sources.tables.references.USER_ACCOUNT
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val dslContext: DSLContext,
) {

    fun findUserByLogin(login: String): UserInfo? {
        return dslContext.select(
            USER_ACCOUNT.FIRST_NAME,
            USER_ACCOUNT.LAST_NAME,
            USER_ACCOUNT.BIRTH_DATE,
            USER_ACCOUNT.EMAIL,
            USER_ACCOUNT.PHONE_NUMBER,
            USER_ACCOUNT.PASSWORD,
            USER_ACCOUNT.SEX,
            condition(DRIVER.ID.isNotNull).`as`("is_driver")
        ).from(
            USER_ACCOUNT.leftJoin(DRIVER).on(USER_ACCOUNT.ID.eq(DRIVER.ID)).where(USER_ACCOUNT.EMAIL.eq(login))
        ).fetchOne()?.toUserInfo()
    }

    private fun Record.toUserInfo(): UserInfo {
        return UserInfo(
            firstName = this.get(USER_ACCOUNT.FIRST_NAME)!!,
            lastName = this.get(USER_ACCOUNT.LAST_NAME)!!,
            birthDate = this.get(USER_ACCOUNT.BIRTH_DATE)!!,
            email = this.get(USER_ACCOUNT.EMAIL)!!,
            phoneNumber = this.get(USER_ACCOUNT.PHONE_NUMBER)!!,
            password = this.get(USER_ACCOUNT.PASSWORD)!!,
            sex = this.get(USER_ACCOUNT.SEX)!!.let { Sex.valueOf(it.name) },
            isDriver = this.get("is_driver") as Boolean,
        )
    }
}