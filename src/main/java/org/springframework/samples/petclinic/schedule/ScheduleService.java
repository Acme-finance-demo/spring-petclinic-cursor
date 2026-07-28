/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.schedule;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing veterinarian schedules.
 *
 * @author Generated
 */
@Service
@Transactional
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;

	public ScheduleService(ScheduleRepository scheduleRepository) {
		this.scheduleRepository = scheduleRepository;
	}

	/**
	 * Find all schedules for a veterinarian.
	 *
	 * @param vetId the veterinarian ID
	 * @return list of schedules
	 */
	@Transactional(readOnly = true)
	public List<Schedule> findSchedulesByVetId(Integer vetId) {
		return scheduleRepository.findByVetId(vetId);
	}

	/**
	 * Find schedules for a veterinarian on a specific day.
	 *
	 * @param vetId the veterinarian ID
	 * @param dayOfWeek the day of week
	 * @return list of schedules
	 */
	@Transactional(readOnly = true)
	public List<Schedule> findSchedulesByVetIdAndDay(Integer vetId, DayOfWeek dayOfWeek) {
		return scheduleRepository.findByVetIdAndDayOfWeek(vetId, dayOfWeek);
	}

	/**
	 * Find a schedule by ID.
	 *
	 * @param scheduleId the schedule ID
	 * @return optional containing the schedule if found
	 */
	@Transactional(readOnly = true)
	public Optional<Schedule> findScheduleById(Integer scheduleId) {
		return scheduleRepository.findById(scheduleId);
	}

	/**
	 * Create a new schedule.
	 *
	 * @param schedule the schedule to create
	 * @return the created schedule
	 */
	public Schedule createSchedule(Schedule schedule) {
		return scheduleRepository.save(schedule);
	}

	/**
	 * Update an existing schedule.
	 *
	 * @param schedule the schedule to update
	 * @return the updated schedule
	 */
	public Schedule updateSchedule(Schedule schedule) {
		return scheduleRepository.save(schedule);
	}

	/**
	 * Delete a schedule.
	 *
	 * @param scheduleId the schedule ID
	 */
	public void deleteSchedule(Integer scheduleId) {
		scheduleRepository.deleteById(scheduleId);
	}

}
