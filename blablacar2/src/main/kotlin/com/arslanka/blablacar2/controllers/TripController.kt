package com.arslanka.blablacar2.controllers

import com.arslanka.blablacar2.api.TripApi
import com.arslanka.blablacar2.model.TripCreateRequest
import com.arslanka.blablacar2.model.TripListRequest
import com.arslanka.blablacar2.model.TripListResponse
import com.arslanka.blablacar2.models.TripInfo
import com.arslanka.blablacar2.models.TripStatus
import com.arslanka.blablacar2.services.TripService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import com.arslanka.blablacar2.model.TripInfo as TripInfoDto
import com.arslanka.blablacar2.model.TripStatus as TripStatusDto

@RestController
@CrossOrigin(origins = ["*"])
class TripController(
    private val tripService: TripService,
) : TripApi {

    override fun v1TripListPost(tripListRequest: TripListRequest): ResponseEntity<TripListResponse> {
        return ResponseEntity.ok(
            TripListResponse(
                trips = tripService.getTripList(
                    login = tripListRequest.login,
                    includeOwner = tripListRequest.includeOwnerTrips ?: true,
                    tripStatusList = tripListRequest.tripStatusesToShow?.map { it.toTripStatus() }
                        ?: TripStatus.entries.toList(),
                ).map { it.toTripInfoDto() }
            )
        )
    }

    override fun v1TripCreatePost(tripCreateRequest: TripCreateRequest): ResponseEntity<Unit> {
        tripService.insertTrip(
            tripInfo = tripCreateRequest.let {
                TripInfo(
                    departureLocation = it.tripInfo.departureLocation!!,
                    arrivalLocation = it.tripInfo.arrivalLocation!!,
                    availableSeats = it.tripInfo.availableSeats!!,
                    date = it.tripInfo.date!!,
                    email = tripCreateRequest.login,
                    description = it.tripInfo.desc,
                    tripStatus = it.tripInfo.tripStatus!!.toTripStatus(),
                )
            }
        )
        return ResponseEntity.ok(Unit)
    }

    private fun TripStatusDto.toTripStatus(): TripStatus {
        return TripStatus.valueOf(this.value)
    }

    private fun TripInfo.toTripInfoDto(): TripInfoDto {
        return TripInfoDto(
            departureLocation = this.departureLocation,
            arrivalLocation = this.arrivalLocation,
            availableSeats = this.availableSeats,
            date = this.date,
            desc = this.description,
            tripStatus = when (this.tripStatus) {
                TripStatus.DRAFT -> TripStatusDto.dRAFT
                TripStatus.WAITING_DRIVER -> TripStatusDto.wAITINGDRIVER
                TripStatus.WAITING_PASSENGER -> TripStatusDto.wAITINGPASSENGER
                TripStatus.READY_FOR_TRIP -> TripStatusDto.rEADYFORTRIP
                TripStatus.IN_PROGRESS -> TripStatusDto.iNPROGRESS
                TripStatus.COMPLETED -> TripStatusDto.cOMPLETED
            },
        )
    }
}