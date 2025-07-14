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
package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing vet schedules and holidays.
 *
 * @author Spring Petclinic Team
 */
@Service
public class ScheduleService {

	private final VetScheduleRepository vetScheduleRepository;
	private final VetHolidayRepository vetHolidayRepository;

	@Autowired
	public ScheduleService(VetScheduleRepository vetScheduleRepository,
						   VetHolidayRepository vetHolidayRepository) {
		this.vetScheduleRepository = vetScheduleRepository;
		this.vetHolidayRepository = vetHolidayRepository;
	}

	// VetSchedule methods

	/**
	 * Save a vet schedule.
	 */
	@Transactional
	public void saveVetSchedule(VetSchedule schedule) throws DataAccessException {
		vetScheduleRepository.save(schedule);
	}

	/**
	 * Find a vet schedule by id.
	 */
	@Transactional(readOnly = true)
	public Optional<VetSchedule> findVetScheduleById(Integer id) throws DataAccessException {
		return vetScheduleRepository.findById(id);
	}

	/**
	 * Find all schedules for a vet.
	 */
	@Transactional(readOnly = true)
	public Collection<VetSchedule> findSchedulesByVetId(Integer vetId) throws DataAccessException {
		return vetScheduleRepository.findByVetId(vetId);
	}

	/**
	 * Find available schedules for a vet.
	 */
	@Transactional(readOnly = true)
	public Collection<VetSchedule> findAvailableSchedulesByVetId(Integer vetId) throws DataAccessException {
		return vetScheduleRepository.findAvailableByVetId(vetId);
	}

	/**
	 * Find a specific schedule for a vet and day.
	 */
	@Transactional(readOnly = true)
	public Optional<VetSchedule> findScheduleByVetIdAndDay(Integer vetId, Integer dayOfWeek) throws DataAccessException {
		return vetScheduleRepository.findByVetIdAndDayOfWeek(vetId, dayOfWeek);
	}

	/**
	 * Check if a vet is available on a specific day.
	 */
	@Transactional(readOnly = true)
	public boolean isVetAvailableOnDay(Integer vetId, Integer dayOfWeek) throws DataAccessException {
		return vetScheduleRepository.isVetAvailableOnDay(vetId, dayOfWeek);
	}

	/**
	 * Delete a vet schedule.
	 */
	@Transactional
	public void deleteVetSchedule(Integer scheduleId) throws DataAccessException {
		vetScheduleRepository.deleteById(scheduleId);
	}

	/**
	 * Delete all schedules for a vet.
	 */
	@Transactional
	public void deleteAllSchedulesForVet(Integer vetId) throws DataAccessException {
		vetScheduleRepository.deleteByVetId(vetId);
	}

	// VetHoliday methods

	/**
	 * Save a vet holiday.
	 */
	@Transactional
	public void saveVetHoliday(VetHoliday holiday) throws DataAccessException {
		vetHolidayRepository.save(holiday);
	}

	/**
	 * Find a vet holiday by id.
	 */
	@Transactional(readOnly = true)
	public Optional<VetHoliday> findVetHolidayById(Integer id) throws DataAccessException {
		return vetHolidayRepository.findById(id);
	}

	/**
	 * Find all holidays for a vet.
	 */
	@Transactional(readOnly = true)
	public Collection<VetHoliday> findHolidaysByVetId(Integer vetId) throws DataAccessException {
		return vetHolidayRepository.findByVetId(vetId);
	}

	/**
	 * Find future holidays for a vet.
	 */
	@Transactional(readOnly = true)
	public Collection<VetHoliday> findFutureHolidaysByVetId(Integer vetId) throws DataAccessException {
		return vetHolidayRepository.findFutureByVetId(vetId);
	}

	/**
	 * Check if a vet is on holiday on a specific date.
	 */
	@Transactional(readOnly = true)
	public boolean isVetOnHoliday(Integer vetId, LocalDate date) throws DataAccessException {
		return vetHolidayRepository.isVetOnHoliday(vetId, date);
	}

	/**
	 * Find holidays by date range.
	 */
	@Transactional(readOnly = true)
	public Collection<VetHoliday> findHolidaysByDateRange(LocalDate startDate, LocalDate endDate) throws DataAccessException {
		return vetHolidayRepository.findByDateRange(startDate, endDate);
	}

	/**
	 * Find holidays for a vet within a date range.
	 */
	@Transactional(readOnly = true)
	public Collection<VetHoliday> findHolidaysByVetIdAndDateRange(Integer vetId, LocalDate startDate, LocalDate endDate) throws DataAccessException {
		return vetHolidayRepository.findByVetIdAndDateRange(vetId, startDate, endDate);
	}

	/**
	 * Find today's holidays.
	 */
	@Transactional(readOnly = true)
	public Collection<VetHoliday> findTodaysHolidays() throws DataAccessException {
		return vetHolidayRepository.findTodaysHolidays();
	}

	/**
	 * Delete a vet holiday.
	 */
	@Transactional
	public void deleteVetHoliday(Integer holidayId) throws DataAccessException {
		vetHolidayRepository.deleteById(holidayId);
	}

	/**
	 * Delete all holidays for a vet.
	 */
	@Transactional
	public void deleteAllHolidaysForVet(Integer vetId) throws DataAccessException {
		vetHolidayRepository.deleteByVetId(vetId);
	}

}