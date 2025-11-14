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
 * Validator for checking appointment slot availability.
 * Manages concurrent appointment limits and emergency slot reservations.
 *
 * @author Spring PetClinic Team
 */
@Component
public class SlotAvailabilityValidator {

	/**
	 * Maximum number of concurrent appointments per veterinarian.
	 */
	private static final int MAX_CONCURRENT_APPOINTMENTS = 3;

	/**
	 * Number of slots to reserve for emergency appointments.
	 */
	private static final int EMERGENCY_SLOTS = 1;

	/**
	 * Checks if there is an available slot for the given veterinarian at the specified
	 * time.
	 * @param vetId the veterinarian ID
	 * @param appointmentTime the appointment time
	 * @param isEmergency whether this is an emergency appointment
	 * @return true if a slot is available, false otherwise
	 */
	public boolean hasAvailableSlot(Integer vetId, LocalDateTime appointmentTime, boolean isEmergency) {
		if (vetId == null || appointmentTime == null) {
			return false;
		}

		// TODO: Implement actual database query to count concurrent appointments
		// This will be implemented when AppointmentRepository is available
		// Query should count appointments where:
		// - vet_id = vetId
		// - appointment time overlaps with the requested time slot

		int currentAppointments = 0; // Placeholder - will be replaced with actual query

		int availableSlots = isEmergency ? MAX_CONCURRENT_APPOINTMENTS
				: (MAX_CONCURRENT_APPOINTMENTS - EMERGENCY_SLOTS);

		return currentAppointments < availableSlots;
	}

	/**
	 * Checks if there is an available slot (non-emergency).
	 * @param vetId the veterinarian ID
	 * @param appointmentTime the appointment time
	 * @return true if a slot is available, false otherwise
	 */
	public boolean hasAvailableSlot(Integer vetId, LocalDateTime appointmentTime) {
		return hasAvailableSlot(vetId, appointmentTime, false);
	}

	/**
	 * Gets the maximum number of concurrent appointments.
	 * @return the maximum concurrent appointments
	 */
	public int getMaxConcurrentAppointments() {
		return MAX_CONCURRENT_APPOINTMENTS;
	}

	/**
	 * Gets the number of emergency slots reserved.
	 * @return the number of emergency slots
	 */
	public int getEmergencySlots() {
		return EMERGENCY_SLOTS;
	}

	/**
	 * Calculates the number of available regular slots.
	 * @return the number of available regular slots
	 */
	public int getAvailableRegularSlots() {
		return MAX_CONCURRENT_APPOINTMENTS - EMERGENCY_SLOTS;
	}

}
