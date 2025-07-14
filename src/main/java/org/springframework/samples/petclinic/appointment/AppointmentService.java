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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing appointments and related business logic.
 *
 * @author Spring Petclinic Team
 */
@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;

	private final VetScheduleRepository vetScheduleRepository;

	private final VetHolidayRepository vetHolidayRepository;

	@Autowired
	public AppointmentService(AppointmentRepository appointmentRepository, VetScheduleRepository vetScheduleRepository,
			VetHolidayRepository vetHolidayRepository) {
		this.appointmentRepository = appointmentRepository;
		this.vetScheduleRepository = vetScheduleRepository;
		this.vetHolidayRepository = vetHolidayRepository;
	}

	/**
	 * Save an appointment to the data store.
	 */
	@Transactional
	public void saveAppointment(Appointment appointment) throws DataAccessException {
		appointmentRepository.save(appointment);
	}

	/**
	 * Retrieve an appointment by id.
	 */
	@Transactional(readOnly = true)
	public Optional<Appointment> findAppointmentById(Integer id) throws DataAccessException {
		return appointmentRepository.findById(id);
	}

	/**
	 * Retrieve all appointments with pagination.
	 */
	@Transactional(readOnly = true)
	public Page<Appointment> findAllAppointments(Pageable pageable) throws DataAccessException {
		return appointmentRepository.findAll(pageable);
	}

	/**
	 * Search appointments by criteria.
	 */
	@Transactional(readOnly = true)
	public Page<Appointment> searchAppointments(LocalDate startDate, LocalDate endDate, Integer vetId,
			AppointmentStatus status, Pageable pageable) throws DataAccessException {
		return appointmentRepository.searchAppointments(startDate, endDate, vetId, status, pageable);
	}

	/**
	 * Retrieve appointments by date range.
	 */
	@Transactional(readOnly = true)
	public Collection<Appointment> findAppointmentsByDateRange(LocalDate startDate, LocalDate endDate)
			throws DataAccessException {
		return appointmentRepository.findByDateRange(startDate, endDate);
	}

	/**
	 * Retrieve appointments by pet id.
	 */
	@Transactional(readOnly = true)
	public Collection<Appointment> findAppointmentsByPetId(Integer petId) throws DataAccessException {
		return appointmentRepository.findByPetId(petId);
	}

	/**
	 * Retrieve appointments by vet id.
	 */
	@Transactional(readOnly = true)
	public Collection<Appointment> findAppointmentsByVetId(Integer vetId) throws DataAccessException {
		return appointmentRepository.findByVetId(vetId);
	}

	/**
	 * Retrieve appointments by vet and date.
	 */
	@Transactional(readOnly = true)
	public Collection<Appointment> findAppointmentsByVetIdAndDate(Integer vetId, LocalDate date)
			throws DataAccessException {
		return appointmentRepository.findByVetIdAndDate(vetId, date);
	}

	/**
	 * Get upcoming appointments.
	 */
	@Transactional(readOnly = true)
	public Collection<Appointment> findUpcomingAppointments() throws DataAccessException {
		return appointmentRepository.findUpcomingAppointments();
	}

	/**
	 * Cancel an appointment.
	 */
	@Transactional
	public void cancelAppointment(Integer appointmentId) throws DataAccessException {
		Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
		if (appointmentOpt.isPresent()) {
			Appointment appointment = appointmentOpt.get();
			appointment.setStatus(AppointmentStatus.CANCELLED);
			appointmentRepository.save(appointment);
		}
	}

	/**
	 * Confirm an appointment.
	 */
	@Transactional
	public void confirmAppointment(Integer appointmentId) throws DataAccessException {
		Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
		if (appointmentOpt.isPresent()) {
			Appointment appointment = appointmentOpt.get();
			appointment.setStatus(AppointmentStatus.CONFIRMED);
			appointmentRepository.save(appointment);
		}
	}

	/**
	 * Complete an appointment.
	 */
	@Transactional
	public void completeAppointment(Integer appointmentId) throws DataAccessException {
		Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
		if (appointmentOpt.isPresent()) {
			Appointment appointment = appointmentOpt.get();
			appointment.setStatus(AppointmentStatus.COMPLETED);
			appointmentRepository.save(appointment);
		}
	}

	/**
	 * Validate if an appointment can be scheduled.
	 */
	@Transactional(readOnly = true)
	public AppointmentValidationResult validateAppointment(Appointment appointment) {
		AppointmentValidationResult result = new AppointmentValidationResult();

		// Check if date is in the past
		if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
			result.addError("Appointment date cannot be in the past");
		}

		// Check vet availability (schedule)
		if (!isVetAvailableOnDateTime(appointment.getVet().getId(), appointment.getAppointmentDate(),
				appointment.getAppointmentTime())) {
			result.addError("Veterinarian is not available at the requested time");
		}

		// Check vet holidays
		if (vetHolidayRepository.isVetOnHoliday(appointment.getVet().getId(), appointment.getAppointmentDate())) {
			result.addError("Veterinarian is on holiday on the requested date");
		}

		// Check for conflicting appointments
		LocalTime appointmentTime = appointment.getAppointmentTime();
		LocalTime endTime = appointmentTime.plusMinutes(appointment.getDurationMinutes());
		if (appointmentTime != null) {
			Integer excludeId = appointment.getId() != null ? appointment.getId() : -1;
			Collection<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
					appointment.getVet().getId(), appointment.getAppointmentDate(), appointmentTime, endTime,
					excludeId);
			if (!conflicts.isEmpty()) {
				result.addError("Time slot conflicts with existing appointment");
			}
		}

		return result;
	}

	/**
	 * Check if a vet is available on a specific date and time.
	 */
	@Transactional(readOnly = true)
	public boolean isVetAvailableOnDateTime(Integer vetId, LocalDate date, LocalTime time) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		int dayOfWeekValue = dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();

		Optional<VetSchedule> scheduleOpt = vetScheduleRepository.findByVetIdAndDayOfWeek(vetId, dayOfWeekValue);
		if (scheduleOpt.isEmpty()) {
			return false;
		}

		VetSchedule schedule = scheduleOpt.get();
		return schedule.isTimeWithinSchedule(time);
	}

	/**
	 * Get available time slots for a vet on a specific date.
	 */
	@Transactional(readOnly = true)
	public Collection<LocalTime> getAvailableTimeSlots(Integer vetId, LocalDate date, int durationMinutes) {
		// This would typically return available time slots based on the vet's schedule
		// and existing appointments. For brevity, we'll return a basic implementation.
		java.util.List<LocalTime> slots = new java.util.ArrayList<>();

		DayOfWeek dayOfWeek = date.getDayOfWeek();
		int dayOfWeekValue = dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();

		Optional<VetSchedule> scheduleOpt = vetScheduleRepository.findByVetIdAndDayOfWeek(vetId, dayOfWeekValue);
		if (scheduleOpt.isEmpty() || vetHolidayRepository.isVetOnHoliday(vetId, date)) {
			return slots;
		}

		VetSchedule schedule = scheduleOpt.get();
		Collection<Appointment> existingAppointments = appointmentRepository.findByVetIdAndDate(vetId, date);

		// Generate time slots (every 30 minutes by default)
		LocalTime currentTime = schedule.getStartTime();
		while (currentTime.plusMinutes(durationMinutes).isBefore(schedule.getEndTime())
				|| currentTime.plusMinutes(durationMinutes).equals(schedule.getEndTime())) {

			boolean isAvailable = true;
			LocalDateTime slotStart = LocalDateTime.of(date, currentTime);
			LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

			// Check conflicts with existing appointments
			for (Appointment existing : existingAppointments) {
				if (existing.getStatus() == AppointmentStatus.CONFIRMED
						|| existing.getStatus() == AppointmentStatus.PENDING) {
					LocalDateTime existingStart = existing.getAppointmentDateTime();
					LocalDateTime existingEnd = existing.getAppointmentEndDateTime();

					if (existingStart != null && existingEnd != null && slotStart.isBefore(existingEnd)
							&& existingStart.isBefore(slotEnd)) {
						isAvailable = false;
						break;
					}
				}
			}

			if (isAvailable) {
				slots.add(currentTime);
			}

			currentTime = currentTime.plusMinutes(30); // 30-minute intervals
		}

		return slots;
	}

	/**
	 * Delete an appointment.
	 */
	@Transactional
	public void deleteAppointment(Integer appointmentId) throws DataAccessException {
		appointmentRepository.deleteById(appointmentId);
	}

}