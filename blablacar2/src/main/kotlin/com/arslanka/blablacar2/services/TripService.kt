package com.arslanka.blablacar2.services

import com.arslanka.blablacar2.models.TripInfo
import com.arslanka.blablacar2.models.TripStatus
import com.arslanka.blablacar2.repositories.TripRepository
import org.springframework.stereotype.Service

@Service
class TripService(
    private val tripRepository: TripRepository,
) {
    fun getTripList(login: String, includeOwner: Boolean, tripStatusList: List<TripStatus>): List<TripInfo> {
        return tripRepository.getTripListFiltered(
            login = login,
            includeOwner = includeOwner,
            tripStatusList = tripStatusList,
        )
    }

    fun insertTrip(tripInfo: TripInfo) {
        tripRepository.insertTripInfo(tripInfo = tripInfo)
    }
}