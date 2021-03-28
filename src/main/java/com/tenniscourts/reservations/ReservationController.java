package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/reservation")
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    @PostMapping(value = "/bookReservation")
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }

    @GetMapping(value = "/findReservation/{reservationId}")
    public ResponseEntity<ReservationDTO> findReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.findReservation(reservationId));
    }

    @PostMapping(value = "/cancelReservation")
    public ResponseEntity<ReservationDTO> cancelReservation(@RequestParam Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }

    @PostMapping(value = "/rescheduleReservation/{reservationId}")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@PathVariable Long reservationId, @RequestParam Long scheduleId) {
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, scheduleId));
    }
}
