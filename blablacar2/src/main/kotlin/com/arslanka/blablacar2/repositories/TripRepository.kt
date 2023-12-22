package com.arslanka.blablacar2.repositories

import com.arslanka.blablacar2.models.Trip
import com.arslanka.blablacar2.models.TripInfo
import com.arslanka.blablacar2.models.TripReply
import com.arslanka.blablacar2.models.TripStatus
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.inline
import org.jooq.sources.enums.TripOwnerType
import org.jooq.sources.tables.references.DRIVER
import org.jooq.sources.tables.references.LOCATION
import org.jooq.sources.tables.references.TRIP
import org.jooq.sources.tables.references.TRIP_DRIVER
import org.jooq.sources.tables.references.TRIP_PASSENGER
import org.jooq.sources.tables.references.USER_ACCOUNT
import org.springframework.stereotype.Repository
import com.arslanka.blablacar2.models.Trip as TripModel

@Repository
class TripRepository(
    private val dslContext: DSLContext,
) {
    fun getTripListFiltered(login: String, includeOwner: Boolean, tripStatusList: List<TripStatus>): List<TripModel> {
        return if (includeOwner) {
            getTripsFiltered(login = login, tripStatusList = tripStatusList)
        } else {
            return dslContext.select(
                TRIP.ID,
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
            ).groupBy(
                LOCATION.`as`("t1").NAME.`as`("departure_location"),
                LOCATION.`as`("t2").NAME.`as`("arrival_location"),
                TRIP.AVAILABLE_SEATS,
                TRIP.DATE,
                TRIP.DESCRIPTION,
                TRIP.STATUS,
                USER_ACCOUNT.EMAIL,
                TRIP.ID,
            ).orderBy(TRIP.DATE.desc()).fetch().map { it.toTrip() }
        }
    }

    fun getTripsFiltered(login: String, tripStatusList: List<TripStatus>): List<TripModel> {
        return dslContext.select(
            TRIP.ID,
            LOCATION.`as`("t1").NAME.`as`("departure_location"),
            LOCATION.`as`("t2").NAME.`as`("arrival_location"),
            TRIP.AVAILABLE_SEATS,
            TRIP.DATE,
            TRIP.DESCRIPTION,
            TRIP.STATUS,
            USER_ACCOUNT.EMAIL,
        ).from(
            TRIP.innerJoin(USER_ACCOUNT)
                .on(TRIP.OWNER_ID.eq(USER_ACCOUNT.ID).and(USER_ACCOUNT.EMAIL.eq(login)))
                .rightJoin(LOCATION.`as`("t1")).on(TRIP.DEPARTURE_LOCATION_ID.eq(LOCATION.`as`("t1").ID))
                .rightJoin(LOCATION.`as`("t2")).on(
                    TRIP.ARRIVAL_LOCATION_ID.eq(
                        LOCATION.`as`("t2").ID
                    )
                ).where(TRIP.STATUS.`in`(tripStatusList.map { it.name }))
        ).groupBy(
            LOCATION.`as`("t1").NAME.`as`("departure_location"),
            LOCATION.`as`("t2").NAME.`as`("arrival_location"),
            TRIP.AVAILABLE_SEATS,
            TRIP.DATE,
            TRIP.DESCRIPTION,
            TRIP.STATUS,
            USER_ACCOUNT.EMAIL,
            TRIP.ID,
        ).orderBy(TRIP.DATE.desc()).fetch().map { it.toTrip() }
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
            dslContext.selectDistinct(
                inline(tripInfo.availableSeats),
                inline(org.jooq.sources.enums.TripStatus.valueOf(tripInfo.tripStatus.name)),
                inline(tripInfo.date),
                inline(tripInfo.description),
                inline(
                    if (isDriver == null) {
                        TripOwnerType.PASSENGER
                    } else {
                        TripOwnerType.DRIVER
                    }
                ),
                USER_ACCOUNT.ID,
                DRIVER.ID,
                LOCATION.`as`("t1").ID,
                LOCATION.`as`("t2").ID,
            ).from(
                USER_ACCOUNT.leftJoin(DRIVER).on(USER_ACCOUNT.ID.eq(DRIVER.ID))
                    .innerJoin(LOCATION.`as`("t1"))
                        .on((LOCATION.`as`("t1").NAME.
                                    eq(tripInfo.departureLocation)))
                    .innerJoin(LOCATION.`as`("t2"))
                        .on((LOCATION.`as`("t2").NAME
                                    .eq(tripInfo.arrivalLocation)))
            ).where(USER_ACCOUNT.EMAIL.eq(tripInfo.email))
        ).execute()
    }

    fun updateTrip(trip: Trip) {
        val record = dslContext.selectDistinct(
            LOCATION.`as`("t1").ID,
            LOCATION.`as`("t2").ID,
        ).from(
            TRIP.innerJoin(USER_ACCOUNT).on(
                TRIP.OWNER_ID.eq(USER_ACCOUNT.ID).and((USER_ACCOUNT.EMAIL).eq(trip.tripInfo.email))
            )
                .innerJoin(LOCATION.`as`("t1"))
                .on((LOCATION.`as`("t1").NAME.eq(trip.tripInfo.departureLocation)))
                .innerJoin(LOCATION.`as`("t2")).on((LOCATION.`as`("t2").NAME.eq(trip.tripInfo.arrivalLocation)))
        ).fetchOne() ?: return

        dslContext.update(TRIP)
            .set(TRIP.DATE, trip.tripInfo.date)
            .set(TRIP.AVAILABLE_SEATS, trip.tripInfo.availableSeats)
            .set(TRIP.DEPARTURE_LOCATION_ID, record.get(LOCATION.`as`("t1").ID))
            .set(TRIP.ARRIVAL_LOCATION_ID, record.get(LOCATION.`as`("t2").ID))
            .set(TRIP.DESCRIPTION, trip.tripInfo.description)
            .where(
                TRIP.ID.eq(trip.tripId)
            ).execute()
    }

    fun replyOnTrip(tripReply: TripReply) {
        val userId = dslContext.select(USER_ACCOUNT.ID).from(USER_ACCOUNT).where(USER_ACCOUNT.EMAIL.eq(tripReply.email))
            .fetchOne()?.get(USER_ACCOUNT.ID) ?: return
        val driverId = dslContext.select(DRIVER.ID).from(DRIVER).where(DRIVER.ID.eq(userId)).fetchOne()

        if (driverId != null) {
            dslContext.insertInto(
                TRIP_DRIVER,
                TRIP_DRIVER.TRIP_ID,
                TRIP_DRIVER.DRIVER_ID
            ).values(
                inline(tripReply.tripId),
                inline(userId),
            ).onDuplicateKeyIgnore().execute()
        } else {
            dslContext.insertInto(
                TRIP_PASSENGER,
                TRIP_PASSENGER.TRIP_ID,
                TRIP_PASSENGER.PASSENGER_ID
            ).values(
                inline(tripReply.tripId),
                inline(userId),
            ).onDuplicateKeyIgnore().execute()
        }
    }

    fun getReplyTripLogins(tripId: Int): List<String> {
        return dslContext.selectDistinct(
            USER_ACCOUNT.EMAIL,
        ).from(
            USER_ACCOUNT.leftJoin(TRIP_PASSENGER).on(
                TRIP_PASSENGER.TRIP_ID.eq(tripId).and(TRIP_PASSENGER.PASSENGER_ID.eq(USER_ACCOUNT.ID))
            ).leftJoin(
                TRIP_DRIVER
            ).on(
                TRIP_DRIVER.TRIP_ID.eq(tripId).and(TRIP_DRIVER.DRIVER_ID.eq(USER_ACCOUNT.ID))
            )
        ).where(TRIP_DRIVER.DRIVER_ID.isNotNull.or(TRIP_PASSENGER.PASSENGER_ID.isNotNull)).fetch().map { it.get((USER_ACCOUNT.EMAIL)) }
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

    fun Record.toTrip(): TripModel {
        return TripModel(
            tripId = this.get(
                TRIP.ID
            )!!,
            tripInfo = this.toTripInfo(),
        )
    }
}