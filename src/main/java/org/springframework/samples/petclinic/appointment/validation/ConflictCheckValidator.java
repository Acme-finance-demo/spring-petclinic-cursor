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
package org.springframework.samples.petclinic.appointment.validation;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validator for checking appointment time conflicts.
 * Ensures that the same veterinarian does not have overlapping appointments.
 *
 * @author Spring PetClinic Team
 */
@Component
public class ConflictCheckValidator {

	/**
	 * Checks if there is a conflicting appointment for the given veterinarian and time range.
	 * @param vetId the veterinarian ID
	 * @param startTime the appointment start time
	 * @param endTime the appointment end time (start time + duration)
	 * @param excludeAppointmentId the appointment ID to exclude from the check (for updates)
	 * @return true if there is a conflict, false otherwise
	 */
	public boolean hasConflictingAppointment(Integer vetId, LocalDateTime startTime, LocalDateTime endTime,
			Integer excludeAppointmentId) {
		if (vetId == null || startTime == null || endTime == null) {
			return false;
		}

		// TODO: Implement actual database query to check for conflicts
		// This will be implemented when AppointmentRepository is available
		// Query should find appointments where:
		// - vet_id = vetId
		// - appointment_id != excludeAppointmentId (if not null)
		// - (startTime < existing.endTime AND endTime > existing.startTime)

		return false;
	}

	/**
	 * Checks if there is a conflicting appointment (without excluding any appointment).
	 * @param vetId the veterinarian ID
	 * @param startTime the appointment start time
	 * @param endTime the appointment end time
	 * @return true if there is a conflict, false otherwise
	 */
	public boolean hasConflictingAppointment(Integer vetId, LocalDateTime startTime, LocalDateTime endTime) {
		return hasConflictingAppointment(vetId, startTime, endTime, null);
	}

}
