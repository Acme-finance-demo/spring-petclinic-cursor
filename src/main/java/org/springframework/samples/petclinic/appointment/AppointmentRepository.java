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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>Appointment</code> domain objects.
 *
 * @author Spring Petclinic Team
 */
public interface AppointmentRepository extends Repository<Appointment, Integer> {

	/**
	 * Save an <code>Appointment</code> to the data store.
	 */
	void save(Appointment appointment) throws DataAccessException;

	/**
	 * Retrieve an <code>Appointment</code> from the data store by id.
	 */
	@Transactional(readOnly = true)
	Optional<Appointment> findById(Integer id) throws DataAccessException;

	/**
	 * Retrieve all <code>Appointment</code>s from the data store.
	 */
	@Transactional(readOnly = true)
	Page<Appointment> findAll(Pageable pageable) throws DataAccessException;

	/**
	 * Retrieve <code>Appointment</code>s by date range.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointmentDate, a.appointmentTime")
	Collection<Appointment> findByDateRange(@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate) throws DataAccessException;

	/**
	 * Retrieve <code>Appointment</code>s by pet id.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE a.pet.id = :petId ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
	Collection<Appointment> findByPetId(@Param("petId") Integer petId) throws DataAccessException;

	/**
	 * Retrieve <code>Appointment</code>s by vet id.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE a.vet.id = :vetId ORDER BY a.appointmentDate, a.appointmentTime")
	Collection<Appointment> findByVetId(@Param("vetId") Integer vetId) throws DataAccessException;

	/**
	 * Retrieve <code>Appointment</code>s by vet id and date.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE a.vet.id = :vetId AND a.appointmentDate = :date ORDER BY a.appointmentTime")
	Collection<Appointment> findByVetIdAndDate(@Param("vetId") Integer vetId, @Param("date") LocalDate date)
			throws DataAccessException;

	/**
	 * Retrieve <code>Appointment</code>s by status.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE a.status = :status ORDER BY a.appointmentDate, a.appointmentTime")
	Collection<Appointment> findByStatus(@Param("status") AppointmentStatus status) throws DataAccessException;

	/**
	 * Find conflicting appointments for a vet on a specific date and time range.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE a.vet.id = :vetId AND a.appointmentDate = :date "
			+ "AND ((FUNCTION('TIMESTAMP', a.appointmentDate, a.appointmentTime) < :endDateTime) "
			+ "AND (FUNCTION('TIMESTAMP', a.appointmentDate, a.appointmentTime) + FUNCTION('INTERVAL', a.durationMinutes, 'MINUTE') > :startDateTime)) "
			+ "AND a.status IN ('PENDING', 'CONFIRMED') " + "AND a.id != :excludeId")
	Collection<Appointment> findConflictingAppointments(@Param("vetId") Integer vetId, @Param("date") LocalDate date,
			@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime,
			@Param("excludeId") Integer excludeId) throws DataAccessException;

	/**
	 * Search appointments by multiple criteria.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE " + "(:startDate IS NULL OR a.appointmentDate >= :startDate) AND "
			+ "(:endDate IS NULL OR a.appointmentDate <= :endDate) AND " + "(:vetId IS NULL OR a.vet.id = :vetId) AND "
			+ "(:status IS NULL OR a.status = :status) " + "ORDER BY a.appointmentDate, a.appointmentTime")
	Page<Appointment> searchAppointments(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
			@Param("vetId") Integer vetId, @Param("status") AppointmentStatus status, Pageable pageable)
			throws DataAccessException;

	/**
	 * Get upcoming appointments (today and future).
	 */
	@Transactional(readOnly = true)
	@Query("SELECT a FROM Appointment a WHERE a.appointmentDate >= CURRENT_DATE "
			+ "AND a.status IN ('PENDING', 'CONFIRMED') " + "ORDER BY a.appointmentDate, a.appointmentTime")
	Collection<Appointment> findUpcomingAppointments() throws DataAccessException;

	/**
	 * Delete an appointment by id.
	 */
	void deleteById(Integer id) throws DataAccessException;

}