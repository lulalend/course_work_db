package com.arslanka.blablacar2.services

import com.arslanka.blablacar2.models.Trip
import com.arslanka.blablacar2.models.TripInfo
import com.arslanka.blablacar2.models.TripReply
import com.arslanka.blablacar2.models.TripStatus
import com.arslanka.blablacar2.repositories.TripRepository
import com.arslanka.blablacar2.utils.Logging
import com.arslanka.blablacar2.utils.logger
import org.springframework.stereotype.Service

@Service
class TripService(
    private val tripRepository: TripRepository,
) {
    private companion object : Logging {
        val log = logger()
    }

    fun getTripList(login: String, includeOwner: Boolean, tripStatusList: List<TripStatus>): List<Trip> {
        log.info("getTripList with login=$login")
        val trips = tripRepository.getTripListFiltered(
            login = login,
            includeOwner = includeOwner,
            tripStatusList = tripStatusList,
        )
        log.info("getTripList returned list with size=${trips.size}")
        return trips
    }

    fun insertTrip(tripInfo: TripInfo) {
        log.info("insertTrip with tripInfo[email=${tripInfo.email}, arrivalLoc=${tripInfo.arrivalLocation}, depLocation=${tripInfo.departureLocation}, availableSeats=${tripInfo.availableSeats}")
        tripRepository.insertTripInfo(tripInfo = tripInfo)
    }

    fun updateTrip(trip: Trip) {
        log.info("updateTrip with tripId=${trip.tripId}, tripInfo[email=${trip.tripInfo.email}, arrivalLoc=${trip.tripInfo.arrivalLocation}, depLocation=${trip.tripInfo.departureLocation}, availableSeats=${trip.tripInfo.availableSeats}, desc=${trip.tripInfo.description}]")
        tripRepository.updateTrip(trip = trip)
    }

    fun replyTrip(tripReply: TripReply) {
        log.info("replyTrip with email=${tripReply.email} and tripId=${tripReply.tripId}")
        tripRepository.replyOnTrip(tripReply = tripReply)
    }

    fun replyTripList(tripId: Int): List<String> {
        log.info("replyTripList with tripId=${tripId}")
        return tripRepository.getReplyTripLogins(tripId = tripId)
    }
}