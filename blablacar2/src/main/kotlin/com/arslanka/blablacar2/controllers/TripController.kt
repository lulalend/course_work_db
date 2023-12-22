package com.arslanka.blablacar2.controllers

import com.arslanka.blablacar2.api.TripApi
import com.arslanka.blablacar2.model.TripCreateRequest
import com.arslanka.blablacar2.model.TripListRequest
import com.arslanka.blablacar2.model.TripListResponse
import com.arslanka.blablacar2.model.TripReplyListRequest
import com.arslanka.blablacar2.model.TripReplyListResponse
import com.arslanka.blablacar2.model.TripReplyRequest
import com.arslanka.blablacar2.model.TripUpdateRequest
import com.arslanka.blablacar2.models.Trip
import com.arslanka.blablacar2.models.TripInfo
import com.arslanka.blablacar2.models.TripReply
import com.arslanka.blablacar2.models.TripStatus
import com.arslanka.blablacar2.services.TripService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import com.arslanka.blablacar2.model.Trip as TripDto
import com.arslanka.blablacar2.model.TripInfo as TripInfoDto
import com.arslanka.blablacar2.model.TripStatus as TripStatusDto

@RestController
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
                ).map { it.toTrip() }
            )
        )
    }

    override fun v1TripCreatePost(tripCreateRequest: TripCreateRequest): ResponseEntity<Unit> {
        tripService.insertTrip(
            tripInfo = tripCreateRequest.let {
                TripInfo(
                    departureLocation = it.tripInfo.departureLocation,
                    arrivalLocation = it.tripInfo.arrivalLocation,
                    availableSeats = it.tripInfo.availableSeats,
                    date = it.tripInfo.date,
                    email = tripCreateRequest.login,
                    description = it.tripInfo.desc,
                    tripStatus = it.tripInfo.tripStatus.toTripStatus(),
                )
            }
        )
        return ResponseEntity.ok(Unit)
    }

    override fun v1TripUpdatePost(tripUpdateRequest: TripUpdateRequest): ResponseEntity<Unit> {
        tripService.updateTrip(
            trip = Trip(
                tripId = tripUpdateRequest.trip.tripId,
                tripInfo = tripUpdateRequest.trip.let {
                    TripInfo(
                        departureLocation = it.tripInfo.departureLocation,
                        arrivalLocation = it.tripInfo.arrivalLocation,
                        availableSeats = it.tripInfo.availableSeats,
                        date = it.tripInfo.date,
                        email = tripUpdateRequest.login,
                        description = it.tripInfo.desc,
                        tripStatus = it.tripInfo.tripStatus.toTripStatus(),
                    )
                }
            )
        )
        return ResponseEntity.ok(Unit)
    }

    override fun v1TripReplyPost(tripReplyRequest: TripReplyRequest): ResponseEntity<Unit> {
        tripService.replyTrip(
            tripReply = TripReply(
                tripId = tripReplyRequest.tripId,
                email = tripReplyRequest.login,
            )
        )
        return ResponseEntity.ok(Unit)
    }

    override fun v1TripReplyListPost(tripReplyListRequest: TripReplyListRequest): ResponseEntity<TripReplyListResponse> {
        return ResponseEntity.ok(
            TripReplyListResponse(
                logins = tripService.replyTripList(tripId = tripReplyListRequest.tripId)
            )
        )
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

    private fun Trip.toTrip(): TripDto {
        return TripDto(
            tripId = this.tripId,
            tripInfo = this.tripInfo.toTripInfoDto(),
        )
    }
}