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

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>VetHoliday</code> domain objects.
 *
 * @author Spring Petclinic Team
 */
public interface VetHolidayRepository extends Repository<VetHoliday, Integer> {

	/**
	 * Save a <code>VetHoliday</code> to the data store.
	 */
	void save(VetHoliday holiday) throws DataAccessException;

	/**
	 * Retrieve a <code>VetHoliday</code> from the data store by id.
	 */
	@Transactional(readOnly = true)
	Optional<VetHoliday> findById(Integer id) throws DataAccessException;

	/**
	 * Retrieve all <code>VetHoliday</code>s for a specific vet.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vh FROM VetHoliday vh WHERE vh.vet.id = :vetId ORDER BY vh.holidayDate")
	Collection<VetHoliday> findByVetId(@Param("vetId") Integer vetId) throws DataAccessException;

	/**
	 * Retrieve future <code>VetHoliday</code>s for a specific vet.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vh FROM VetHoliday vh WHERE vh.vet.id = :vetId AND vh.holidayDate >= CURRENT_DATE ORDER BY vh.holidayDate")
	Collection<VetHoliday> findFutureByVetId(@Param("vetId") Integer vetId) throws DataAccessException;

	/**
	 * Check if a vet is on holiday on a specific date.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT COUNT(vh) > 0 FROM VetHoliday vh WHERE vh.vet.id = :vetId AND vh.holidayDate = :date")
	boolean isVetOnHoliday(@Param("vetId") Integer vetId, @Param("date") LocalDate date) throws DataAccessException;

	/**
	 * Retrieve <code>VetHoliday</code>s by date range.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vh FROM VetHoliday vh WHERE vh.holidayDate BETWEEN :startDate AND :endDate ORDER BY vh.holidayDate, vh.vet.lastName")
	Collection<VetHoliday> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate) throws DataAccessException;

	/**
	 * Retrieve <code>VetHoliday</code>s for a specific vet and date range.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vh FROM VetHoliday vh WHERE vh.vet.id = :vetId AND vh.holidayDate BETWEEN :startDate AND :endDate ORDER BY vh.holidayDate")
	Collection<VetHoliday> findByVetIdAndDateRange(@Param("vetId") Integer vetId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate) throws DataAccessException;

	/**
	 * Retrieve all holidays for today.
	 */
	@Transactional(readOnly = true)
	@Query("SELECT vh FROM VetHoliday vh WHERE vh.holidayDate = CURRENT_DATE ORDER BY vh.vet.lastName, vh.vet.firstName")
	Collection<VetHoliday> findTodaysHolidays() throws DataAccessException;

	/**
	 * Delete a holiday by id.
	 */
	void deleteById(Integer id) throws DataAccessException;

	/**
	 * Delete all holidays for a vet.
	 */
	@Query("DELETE FROM VetHoliday vh WHERE vh.vet.id = :vetId")
	void deleteByVetId(@Param("vetId") Integer vetId) throws DataAccessException;

}