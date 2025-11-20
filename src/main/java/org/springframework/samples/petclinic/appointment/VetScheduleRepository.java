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

import java.util.Collection;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>VetSchedule</code> domain objects.
 *
 * @author Spring Petclinic Team
 */
public interface VetScheduleRepository extends Repository<VetSchedule, Integer> {

	/**
	 * Save a <code>VetSchedule</code> to the data store.
	 */
	void save(VetSchedule schedule) throws DataAccessException;

	/**
	 * Retrieve a <code>VetSchedule</code> from the data store by id.
	 */
	@Transactional(readOnly = true)
	Optional<VetSchedule> findById(Integer id) throws DataAccessException;

	/**
	 * Retrieve all <code>VetSchedule</code>s for a specific vet.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vs FROM VetSchedule vs WHERE vs.vet.id = :vetId ORDER BY vs.dayOfWeek")
	Collection<VetSchedule> findByVetId(@Param("vetId") Integer vetId) throws DataAccessException;

	/**
	 * Retrieve a <code>VetSchedule</code> for a specific vet and day of week.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vs FROM VetSchedule vs WHERE vs.vet.id = :vetId AND vs.dayOfWeek = :dayOfWeek")
	Optional<VetSchedule> findByVetIdAndDayOfWeek(@Param("vetId") Integer vetId, @Param("dayOfWeek") Integer dayOfWeek)
			throws DataAccessException;

	/**
	 * Retrieve all available <code>VetSchedule</code>s for a specific vet.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vs FROM VetSchedule vs WHERE vs.vet.id = :vetId AND vs.isAvailable = true ORDER BY vs.dayOfWeek")
	Collection<VetSchedule> findAvailableByVetId(@Param("vetId") Integer vetId) throws DataAccessException;

	/**
	 * Retrieve all <code>VetSchedule</code>s for a specific day of week.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vs FROM VetSchedule vs WHERE vs.dayOfWeek = :dayOfWeek AND vs.isAvailable = true ORDER BY vs.vet.lastName, vs.vet.firstName")
	Collection<VetSchedule> findByDayOfWeek(@Param("dayOfWeek") Integer dayOfWeek) throws DataAccessException;

	/**
	 * Check if a vet is available on a specific day of week.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT COUNT(vs) > 0 FROM VetSchedule vs WHERE vs.vet.id = :vetId AND vs.dayOfWeek = :dayOfWeek AND vs.isAvailable = true")
	boolean isVetAvailableOnDay(@Param("vetId") Integer vetId, @Param("dayOfWeek") Integer dayOfWeek)
			throws DataAccessException;

	/**
	 * Delete a schedule by id.
	 */
	void deleteById(Integer id) throws DataAccessException;

	/**
	 * Delete all schedules for a vet.
	 */
	@Query("DELETE FROM VetSchedule vs WHERE vs.vet.id = :vetId")
	void deleteByVetId(@Param("vetId") Integer vetId) throws DataAccessException;

}