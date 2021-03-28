package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.BusinessException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleDTO;
import com.tenniscourts.schedules.ScheduleService;
import com.tenniscourts.tenniscourts.TennisCourtService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScheduleService scheduleService;

    private final ReservationMapper reservationMapper;

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        Reservation reservation = reservationMapper.map(createReservationRequestDTO);
        reservation.setValue(new BigDecimal(10));

        this.validateReservation(reservation.getSchedule());
        this.validateScheduleDate(reservation);

        return reservationMapper.map(reservationRepository.save(reservation));
    }

    private void validateReservation(final Schedule schedule) {
        findAll().stream().forEach(it -> {
            if (it.getSchedule().getId().equals(schedule.getId())) {
                throw new IllegalArgumentException("Schedule already booked.");
            }
        });
    }

    private void validateScheduleDate(Reservation reservation) {
        ScheduleDTO schedule = scheduleService.findSchedule(reservation.getSchedule().getId());
        if (schedule != null && schedule.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book reservation to past dates.");
        }
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservationMapper::map).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(reservationId));
    }

    private Reservation cancel(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {

            this.validateCancellation(reservation);

            BigDecimal refundValue = getRefundValue(reservation);
            return this.updateReservation(reservation, refundValue, ReservationStatus.CANCELLED);

        }).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue, ReservationStatus status) {
        reservation.setReservationStatus(status);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }

        if (reservation.getSchedule().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }

    }

    public BigDecimal getRefundValue(Reservation reservation) {
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), reservation.getSchedule().getStartDateTime());

        if (hours >= 24) {
            return reservation.getValue();
        }

        return BigDecimal.ZERO;
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long scheduleId) {
        reservationRepository.findById(previousReservationId).map(it -> {
            if (scheduleId.equals(it.getSchedule().getId()))
                throw new IllegalArgumentException("Cannot reschedule to the same slot.");
            return scheduleId;
        });

        Reservation previousReservation = cancel(previousReservationId);
        previousReservation.setReservationStatus(ReservationStatus.RESCHEDULED);
        reservationRepository.save(previousReservation);

        ReservationDTO newReservation = bookReservation(CreateReservationRequestDTO.builder()
                .guestId(previousReservation.getGuest().getId())
                .scheduleId(scheduleId)
                .build());
        newReservation.setPreviousReservation(reservationMapper.map(previousReservation));
        return newReservation;
    }

    public List<ReservationDTO> findAll() {
        return reservationMapper.map(reservationRepository.findAll());
    }
}
