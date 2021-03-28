package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final ScheduleMapper scheduleMapper;

    public ScheduleDTO addSchedule(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        Schedule schedule = scheduleMapper.map(createScheduleRequestDTO);

        if (findSchedulesByTennisCourtId(tennisCourtId).stream().anyMatch(it -> evaluateDates(it, schedule))) {
            throw new AlreadyExistsEntityException("Schedule already registered or conflicting.");
        }

        val endTime = schedule.getStartDateTime().plusHours(1);
        schedule.setEndDateTime(endTime);
        return scheduleMapper.map(scheduleRepository.save(schedule));
    }

    private boolean evaluateDates(ScheduleDTO it, Schedule schedule) {
        return it.getStartDateTime().isEqual(schedule.getStartDateTime())
                || (schedule.getStartDateTime().isAfter(it.getStartDateTime()) && schedule.getStartDateTime().isBefore(it.getEndDateTime()))
                || (schedule.getStartDateTime().plusHours(1).isAfter(it.getStartDateTime()) && schedule.getStartDateTime().isBefore(it.getEndDateTime()));
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleMapper.map(scheduleRepository.findAllByStartDateTimeIsGreaterThanEqualAndEndDateTimeIsLessThanEqual(startDate, endDate));
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        return scheduleMapper.map(scheduleRepository.findById(scheduleId).<EntityNotFoundException>orElseThrow(() -> {
            throw new EntityNotFoundException("Schedule not found.");
        }));
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }
}
