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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Validator for checking if appointments are within business hours.
 * Default business hours:
 * - Weekdays (Mon-Fri): 9:00 AM - 6:00 PM
 * - Saturday: 9:00 AM - 1:00 PM
 * - Sunday: Closed
 *
 * @author Spring PetClinic Team
 */
@Component
public class BusinessHoursValidator {

	private static final LocalTime WEEKDAY_OPEN = LocalTime.of(9, 0);

	private static final LocalTime WEEKDAY_CLOSE = LocalTime.of(18, 0);

	private static final LocalTime SATURDAY_OPEN = LocalTime.of(9, 0);

	private static final LocalTime SATURDAY_CLOSE = LocalTime.of(13, 0);

	/**
	 * Checks if the given appointment time is within business hours.
	 * @param appointmentTime the appointment time to check
	 * @return true if within business hours, false otherwise
	 */
	public boolean isWithinBusinessHours(LocalDateTime appointmentTime) {
		if (appointmentTime == null) {
			return false;
		}

		DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();
		LocalTime time = appointmentTime.toLocalTime();

		return switch (dayOfWeek) {
			case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> isWithinTimeRange(time, WEEKDAY_OPEN, WEEKDAY_CLOSE);
			case SATURDAY -> isWithinTimeRange(time, SATURDAY_OPEN, SATURDAY_CLOSE);
			case SUNDAY -> false; // Closed on Sundays
		};
	}

	/**
	 * Checks if the given time is within the specified time range.
	 * @param time the time to check
	 * @param openTime the opening time
	 * @param closeTime the closing time
	 * @return true if within range, false otherwise
	 */
	private boolean isWithinTimeRange(LocalTime time, LocalTime openTime, LocalTime closeTime) {
		return !time.isBefore(openTime) && time.isBefore(closeTime);
	}

	/**
	 * Gets the business hours for a specific day of week.
	 * @param dayOfWeek the day of week
	 * @return a string representation of business hours
	 */
	public String getBusinessHours(DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
			case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> WEEKDAY_OPEN + " - " + WEEKDAY_CLOSE;
			case SATURDAY -> SATURDAY_OPEN + " - " + SATURDAY_CLOSE;
			case SUNDAY -> "Closed";
		};
	}

}
