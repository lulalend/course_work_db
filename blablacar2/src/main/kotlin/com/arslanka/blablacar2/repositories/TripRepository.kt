package com.arslanka.blablacar2.repositories

import com.arslanka.blablacar2.models.TripInfo
import com.arslanka.blablacar2.models.TripStatus
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.sources.enums.TripOwnerType
import org.jooq.sources.tables.references.DRIVER
import org.jooq.sources.tables.references.LOCATION
import org.jooq.sources.tables.references.TRIP
import org.jooq.sources.tables.references.USER_ACCOUNT
import org.springframework.stereotype.Repository

@Repository
class TripRepository(
    private val dslContext: DSLContext,
) {
    fun getTripListFiltered(login: String, includeOwner: Boolean, tripStatusList: List<TripStatus>): List<TripInfo> {
        return if (includeOwner) {
            getTripsFiltered(login = login, tripStatusList = tripStatusList)
        } else {
            return dslContext.select(
                LOCATION.`as`("t1").NAME.`as`("departure_location"),
                LOCATION.`as`("t2").NAME.`as`("arrival_location"),
                TRIP.AVAILABLE_SEATS,
                TRIP.DATE,
                TRIP.DESCRIPTION,
                TRIP.STATUS,
                USER_ACCOUNT.EMAIL,
            ).from(
                TRIP.innerJoin(USER_ACCOUNT)
                    .on(TRIP.OWNER_ID.notEqual(USER_ACCOUNT.ID).and(USER_ACCOUNT.EMAIL.eq(login)))
                    .rightJoin(LOCATION.`as`("t1")).on(TRIP.DEPARTURE_LOCATION_ID.eq(LOCATION.`as`("t1").ID))
                    .rightJoin(LOCATION.`as`("t2")).on(
                        TRIP.ARRIVAL_LOCATION_ID.eq(
                            LOCATION.`as`("t2").ID
                        )
                    )
                    .where(TRIP.STATUS.`in`(tripStatusList.map { it.name }))
            ).fetch().map { it.toTripInfo() }
        }
    }

    fun getTripsFiltered(login: String, tripStatusList: List<TripStatus>): List<TripInfo> {
        return dslContext.select(
            LOCATION.`as`("t1").NAME.`as`("departure_location"),
            LOCATION.`as`("t2").NAME.`as`("arrival_location"),
            TRIP.AVAILABLE_SEATS,
            TRIP.DATE,
            TRIP.DESCRIPTION,
            TRIP.STATUS,
            USER_ACCOUNT.EMAIL
        ).from(
            TRIP.innerJoin(USER_ACCOUNT)
                .on(TRIP.OWNER_ID.eq(USER_ACCOUNT.ID).and(USER_ACCOUNT.EMAIL.eq(login)))
                .rightJoin(LOCATION.`as`("t1")).on(TRIP.DEPARTURE_LOCATION_ID.eq(LOCATION.`as`("t1").ID))
                .rightJoin(LOCATION.`as`("t2")).on(
                    TRIP.ARRIVAL_LOCATION_ID.eq(
                        LOCATION.`as`("t2").ID
                    )
                ).where(TRIP.STATUS.`in`(tripStatusList.map { it.name }))
        ).fetch().map { it.toTripInfo() }
    }

    fun insertTripInfo(tripInfo: TripInfo) {
        val isDriver =
            dslContext.select(DRIVER.ID).from(USER_ACCOUNT).leftJoin(DRIVER).on(USER_ACCOUNT.ID.eq(DRIVER.ID)).where(
                USER_ACCOUNT.EMAIL.eq(tripInfo.email)
            ).fetchOne()?.get(DRIVER.ID)
        dslContext.insertInto(
            TRIP,
            TRIP.AVAILABLE_SEATS,
            TRIP.STATUS,
            TRIP.DATE,
            TRIP.DESCRIPTION,
            TRIP.OWNER_TYPE,
            TRIP.OWNER_ID,
            TRIP.DRIVER_ID,
            TRIP.DEPARTURE_LOCATION_ID,
            TRIP.ARRIVAL_LOCATION_ID,
        ).select(
            dslContext.select(
                TRIP.AVAILABLE_SEATS.convertFrom { tripInfo.availableSeats },
                TRIP.STATUS.convertFrom { org.jooq.sources.enums.TripStatus.valueOf(tripInfo.tripStatus.name) },
                TRIP.DATE.convertFrom { tripInfo.date },
                TRIP.DESCRIPTION.convertFrom { tripInfo.description },
                choose().`when`(
                    DRIVER.ID.isNull,
                    org.jooq.sources.enums.TripOwnerType.valueOf("PASSENGER")
                )
                    .otherwise(org.jooq.sources.enums.TripOwnerType.valueOf("DRIVER")),
                USER_ACCOUNT.ID,
                DRIVER.ID,
                LOCATION.`as`("t1").ID,
                LOCATION.`as`("t2").ID,
            ).from(
                USER_ACCOUNT.leftJoin(DRIVER).on(USER_ACCOUNT.ID.eq(DRIVER.ID))
                    .innerJoin(LOCATION.`as`("t1"))
                    .on((LOCATION.`as`("t1").NAME.eq(tripInfo.departureLocation)))
                    .innerJoin(LOCATION.`as`("t2")).on((LOCATION.`as`("t2").NAME.eq(tripInfo.arrivalLocation)))
            )
        ).execute()
    }

    fun Record.toTripInfo(): TripInfo {
        return TripInfo(
            departureLocation = this.get(LOCATION.`as`("t1").NAME.`as`("departure_location"))!!,
            arrivalLocation = this.get(LOCATION.`as`("t2").NAME.`as`("arrival_location"))!!,
            availableSeats = this.get(TRIP.AVAILABLE_SEATS)!!,
            date = this.get(TRIP.DATE)!!,
            description = this.get(TRIP.DESCRIPTION)!!,
            email = this.get(USER_ACCOUNT.EMAIL)!!,
            tripStatus = TripStatus.valueOf(this.get(TRIP.STATUS)!!.literal),
        )
    }
}