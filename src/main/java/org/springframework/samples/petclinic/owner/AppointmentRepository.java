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
package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository class for {@link Appointment} domain objects All method names are kept the same
 * as in the original interface.
 *
 * @author Spring Petclinic Team
 */
public interface AppointmentRepository extends CrudRepository<Appointment, Integer> {

	/**
	 * Retrieve {@link Appointment}s from the data store by date.
	 * @param date the appointment date
	 * @return a {@link List} of matching {@link Appointment}s (or an empty {@link List})
	 */
	@Query("SELECT a FROM Appointment a WHERE CAST(a.dateTime AS date) = :date ORDER BY a.dateTime")
	List<Appointment> findByDate(@Param("date") LocalDate date);

}
