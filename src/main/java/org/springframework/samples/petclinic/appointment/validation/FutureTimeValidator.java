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
import java.time.temporal.ChronoUnit;

/**
 * Validator for checking if appointment time is in the future and meets minimum advance
 * booking requirements. Appointments must be at least 1 hour in advance.
 *
 * @author Spring PetClinic Team
 */
@Component
public class FutureTimeValidator {

	private static final long MINIMUM_ADVANCE_HOURS = 1;

	/**
	 * Checks if the appointment time is in the past.
	 * @param appointmentTime the appointment time to check
	 * @return true if the time is in the past, false otherwise
	 */
	public boolean isPastTime(LocalDateTime appointmentTime) {
		if (appointmentTime == null) {
			return true;
		}
		return appointmentTime.isBefore(LocalDateTime.now());
	}

	/**
	 * Checks if the appointment time meets the minimum advance booking requirement.
	 * Appointments must be at least 1 hour in advance.
	 * @param appointmentTime the appointment time to check
	 * @return true if the time is valid (at least 1 hour in advance), false otherwise
	 */
	public boolean isValidFutureTime(LocalDateTime appointmentTime) {
		if (appointmentTime == null) {
			return false;
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime minimumTime = now.plus(MINIMUM_ADVANCE_HOURS, ChronoUnit.HOURS);

		return appointmentTime.isAfter(minimumTime);
	}

	/**
	 * Gets the minimum advance booking time in hours.
	 * @return the minimum advance booking hours
	 */
	public long getMinimumAdvanceHours() {
		return MINIMUM_ADVANCE_HOURS;
	}

	/**
	 * Calculates the hours between now and the appointment time.
	 * @param appointmentTime the appointment time
	 * @return the number of hours between now and the appointment time
	 */
	public long getHoursUntilAppointment(LocalDateTime appointmentTime) {
		if (appointmentTime == null) {
			return 0;
		}
		return ChronoUnit.HOURS.between(LocalDateTime.now(), appointmentTime);
	}

}
