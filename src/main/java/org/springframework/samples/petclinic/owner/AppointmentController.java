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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Spring Petclinic Team
 */
@Controller
@RequestMapping("/appointments")
class AppointmentController {

	private final AppointmentRepository appointments;

	public AppointmentController(AppointmentRepository appointments) {
		this.appointments = appointments;
	}

	/**
	 * Display appointment calendar page
	 */
	@GetMapping("/calendar")
	public String showCalendar(Model model) {
		return "appointments/appointmentCalendar";
	}

	/**
	 * Get appointments for a specific date (AJAX endpoint)
	 */
	@GetMapping("/by-date")
	@ResponseBody
	public List<Appointment> getAppointmentsByDate(
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		return appointments.findByDate(date);
	}

}
