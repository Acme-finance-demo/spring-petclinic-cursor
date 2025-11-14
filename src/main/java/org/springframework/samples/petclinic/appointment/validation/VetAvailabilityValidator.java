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
 * Validator for checking veterinarian availability.
 * Checks if a veterinarian is working at the requested appointment time.
 *
 * @author Spring PetClinic Team
 */
@Component
public class VetAvailabilityValidator {

	/**
	 * Checks if the veterinarian is available at the given appointment time.
	 * @param vetId the veterinarian ID
	 * @param appointmentTime the appointment time
	 * @return true if the veterinarian is available, false otherwise
	 */
	public boolean isVetAvailable(Integer vetId, LocalDateTime appointmentTime) {
		if (vetId == null || appointmentTime == null) {
			return false;
		}

		// TODO: Implement actual database query to check vet schedule
		// This will be implemented when VetSchedule entity and repository are available
		// Query should find vet schedule where:
		// - vet_id = vetId
		// - date = appointmentTime.toLocalDate()
		// - appointmentTime is within the vet's working hours
		// - vet is not on leave/holiday

		// For now, assume all vets are available (will be updated)
		return true;
	}

	/**
	 * Checks if the veterinarian is on leave on the given date.
	 * @param vetId the veterinarian ID
	 * @param appointmentTime the appointment time
	 * @return true if the veterinarian is on leave, false otherwise
	 */
	public boolean isVetOnLeave(Integer vetId, LocalDateTime appointmentTime) {
		if (vetId == null || appointmentTime == null) {
			return false;
		}

		// TODO: Implement actual database query to check vet leave schedule
		// This will be implemented when VetLeave entity and repository are available

		return false;
	}

}
