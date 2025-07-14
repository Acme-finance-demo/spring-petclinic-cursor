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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controller for appointment management.
 *
 * @author Spring Petclinic Team
 */
@Controller
public class AppointmentController {

	private static final String VIEWS_APPOINTMENT_LIST = "appointments/appointmentList";
	private static final String VIEWS_APPOINTMENT_FORM = "appointments/appointmentForm";
	private static final String VIEWS_APPOINTMENT_DETAILS = "appointments/appointmentDetails";
	private static final String VIEWS_APPOINTMENT_CALENDAR = "appointments/appointmentCalendar";

	private final AppointmentService appointmentService;
	private final OwnerRepository ownerRepository;
	private final PetRepository petRepository;
	private final VetRepository vetRepository;

	@Autowired
	public AppointmentController(AppointmentService appointmentService,
								 OwnerRepository ownerRepository,
								 PetRepository petRepository,
								 VetRepository vetRepository) {
		this.appointmentService = appointmentService;
		this.ownerRepository = ownerRepository;
		this.petRepository = petRepository;
		this.vetRepository = vetRepository;
	}

	/**
	 * Display appointment list with search functionality.
	 */
	@GetMapping("/appointments")
	public String showAppointmentList(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(required = false) String startDate,
		@RequestParam(required = false) String endDate,
		@RequestParam(required = false) Integer vetId,
		@RequestParam(required = false) AppointmentStatus status,
		Model model) {

		// Parse dates
		LocalDate start = null;
		LocalDate end = null;
		try {
			if (startDate != null && !startDate.isEmpty()) {
				start = LocalDate.parse(startDate);
			}
			if (endDate != null && !endDate.isEmpty()) {
				end = LocalDate.parse(endDate);
			}
		} catch (Exception e) {
			// Invalid date format, ignore
		}

		// Set default date range if not specified
		if (start == null && end == null) {
			start = LocalDate.now().minusDays(7);
			end = LocalDate.now().plusDays(30);
		}

		Pageable pageable = PageRequest.of(page, 10);
		Page<Appointment> appointments = appointmentService.searchAppointments(start, end, vetId, status, pageable);

		model.addAttribute("appointments", appointments);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", appointments.getTotalPages());
		model.addAttribute("startDate", start != null ? start.toString() : "");
		model.addAttribute("endDate", end != null ? end.toString() : "");
		model.addAttribute("selectedVetId", vetId);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("vets", vetRepository.findAll());
		model.addAttribute("statuses", AppointmentStatus.values());

		return VIEWS_APPOINTMENT_LIST;
	}

	/**
	 * Display new appointment form.
	 */
	@GetMapping("/appointments/new")
	public String showNewAppointmentForm(
		@RequestParam(required = false) Integer petId,
		Model model) {

		Appointment appointment = new Appointment();
		
		// Pre-select pet if provided
		if (petId != null) {
			Optional<Pet> petOpt = petRepository.findById(petId);
			if (petOpt.isPresent()) {
				appointment.setPet(petOpt.get());
			}
		}

		model.addAttribute("appointment", appointment);
		model.addAttribute("vets", vetRepository.findAll());
		model.addAttribute("pets", petRepository.findAll());
		model.addAttribute("owners", ownerRepository.findAll());

		return VIEWS_APPOINTMENT_FORM;
	}

	/**
	 * Process new appointment form submission.
	 */
	@PostMapping("/appointments/new")
	public String processNewAppointmentForm(
		@Valid @ModelAttribute Appointment appointment,
		BindingResult result,
		RedirectAttributes redirectAttributes,
		Model model) {

		// Validate business rules
		if (!result.hasErrors()) {
			AppointmentValidationResult validation = appointmentService.validateAppointment(appointment);
			if (!validation.isValid()) {
				for (String error : validation.getErrors()) {
					result.rejectValue("appointmentDate", "error.appointment", error);
				}
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("vets", vetRepository.findAll());
			model.addAttribute("pets", petRepository.findAll());
			model.addAttribute("owners", ownerRepository.findAll());
			return VIEWS_APPOINTMENT_FORM;
		}

		appointmentService.saveAppointment(appointment);
		redirectAttributes.addFlashAttribute("message", "Appointment created successfully!");
		return "redirect:/appointments/" + appointment.getId();
	}

	/**
	 * Display appointment details.
	 */
	@GetMapping("/appointments/{appointmentId}")
	public String showAppointmentDetails(@PathVariable Integer appointmentId, Model model) {
		Optional<Appointment> appointmentOpt = appointmentService.findAppointmentById(appointmentId);
		
		if (appointmentOpt.isEmpty()) {
			return "redirect:/appointments";
		}

		model.addAttribute("appointment", appointmentOpt.get());
		return VIEWS_APPOINTMENT_DETAILS;
	}

	/**
	 * Display edit appointment form.
	 */
	@GetMapping("/appointments/{appointmentId}/edit")
	public String showEditAppointmentForm(@PathVariable Integer appointmentId, Model model) {
		Optional<Appointment> appointmentOpt = appointmentService.findAppointmentById(appointmentId);
		
		if (appointmentOpt.isEmpty()) {
			return "redirect:/appointments";
		}

		model.addAttribute("appointment", appointmentOpt.get());
		model.addAttribute("vets", vetRepository.findAll());
		model.addAttribute("pets", petRepository.findAll());
		model.addAttribute("owners", ownerRepository.findAll());

		return VIEWS_APPOINTMENT_FORM;
	}

	/**
	 * Process edit appointment form submission.
	 */
	@PostMapping("/appointments/{appointmentId}/edit")
	public String processEditAppointmentForm(
		@PathVariable Integer appointmentId,
		@Valid @ModelAttribute Appointment appointment,
		BindingResult result,
		RedirectAttributes redirectAttributes,
		Model model) {

		appointment.setId(appointmentId);

		// Validate business rules
		if (!result.hasErrors()) {
			AppointmentValidationResult validation = appointmentService.validateAppointment(appointment);
			if (!validation.isValid()) {
				for (String error : validation.getErrors()) {
					result.rejectValue("appointmentDate", "error.appointment", error);
				}
			}
		}

		if (result.hasErrors()) {
			model.addAttribute("vets", vetRepository.findAll());
			model.addAttribute("pets", petRepository.findAll());
			model.addAttribute("owners", ownerRepository.findAll());
			return VIEWS_APPOINTMENT_FORM;
		}

		appointmentService.saveAppointment(appointment);
		redirectAttributes.addFlashAttribute("message", "Appointment updated successfully!");
		return "redirect:/appointments/" + appointmentId;
	}

	/**
	 * Confirm an appointment.
	 */
	@PostMapping("/appointments/{appointmentId}/confirm")
	public String confirmAppointment(@PathVariable Integer appointmentId, RedirectAttributes redirectAttributes) {
		appointmentService.confirmAppointment(appointmentId);
		redirectAttributes.addFlashAttribute("message", "Appointment confirmed!");
		return "redirect:/appointments/" + appointmentId;
	}

	/**
	 * Cancel an appointment.
	 */
	@PostMapping("/appointments/{appointmentId}/cancel")
	public String cancelAppointment(@PathVariable Integer appointmentId, RedirectAttributes redirectAttributes) {
		appointmentService.cancelAppointment(appointmentId);
		redirectAttributes.addFlashAttribute("message", "Appointment cancelled!");
		return "redirect:/appointments/" + appointmentId;
	}

	/**
	 * Complete an appointment.
	 */
	@PostMapping("/appointments/{appointmentId}/complete")
	public String completeAppointment(@PathVariable Integer appointmentId, RedirectAttributes redirectAttributes) {
		appointmentService.completeAppointment(appointmentId);
		redirectAttributes.addFlashAttribute("message", "Appointment completed!");
		return "redirect:/appointments/" + appointmentId;
	}

	/**
	 * Delete an appointment.
	 */
	@PostMapping("/appointments/{appointmentId}/delete")
	public String deleteAppointment(@PathVariable Integer appointmentId, RedirectAttributes redirectAttributes) {
		appointmentService.deleteAppointment(appointmentId);
		redirectAttributes.addFlashAttribute("message", "Appointment deleted!");
		return "redirect:/appointments";
	}

	/**
	 * Display appointment calendar view.
	 */
	@GetMapping("/appointments/calendar")
	public String showAppointmentCalendar(
		@RequestParam(required = false) String date,
		@RequestParam(required = false) Integer vetId,
		Model model) {

		LocalDate selectedDate = LocalDate.now();
		try {
			if (date != null && !date.isEmpty()) {
				selectedDate = LocalDate.parse(date);
			}
		} catch (Exception e) {
			// Invalid date format, use current date
		}

		// Get appointments for the selected month
		LocalDate monthStart = selectedDate.withDayOfMonth(1);
		LocalDate monthEnd = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth());
		
		Collection<Appointment> appointments;
		if (vetId != null) {
			appointments = appointmentService.findAppointmentsByDateRange(monthStart, monthEnd)
				.stream()
				.filter(a -> a.getVet().getId().equals(vetId))
				.toList();
		} else {
			appointments = appointmentService.findAppointmentsByDateRange(monthStart, monthEnd);
		}

		// Get appointments for the selected date
		Collection<Appointment> dayAppointments = appointments.stream()
			.filter(a -> a.getAppointmentDate().equals(selectedDate))
			.toList();

		model.addAttribute("selectedDate", selectedDate);
		model.addAttribute("monthStart", monthStart);
		model.addAttribute("monthEnd", monthEnd);
		model.addAttribute("appointments", appointments);
		model.addAttribute("dayAppointments", dayAppointments);
		model.addAttribute("vets", vetRepository.findAll());
		model.addAttribute("selectedVetId", vetId);

		return VIEWS_APPOINTMENT_CALENDAR;
	}

	/**
	 * Add appointments link to pet details by owner.
	 */
	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments")
	public String showPetAppointments(
		@PathVariable Integer ownerId,
		@PathVariable Integer petId,
		Model model) {

		Collection<Appointment> appointments = appointmentService.findAppointmentsByPetId(petId);
		Optional<Pet> petOpt = petRepository.findById(petId);
		
		if (petOpt.isEmpty()) {
			return "redirect:/owners/" + ownerId;
		}

		model.addAttribute("appointments", appointments);
		model.addAttribute("pet", petOpt.get());
		model.addAttribute("owner", petOpt.get().getOwner());

		return "appointments/petAppointments";
	}

}