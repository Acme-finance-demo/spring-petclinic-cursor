/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class AppointmentController {

	private final AppointmentRepository appointments;

	private final OwnerRepository owners;

	private final PetRepository pets;

	private final VetRepository vets;

	public AppointmentController(AppointmentRepository appointments, OwnerRepository owners, PetRepository pets,
			VetRepository vets) {
		this.appointments = appointments;
		this.owners = owners;
		this.pets = pets;
		this.vets = vets;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @GetMapping or @PostMapping annotated method. 2
	 * goals: - Make sure we always have fresh data - Since we do not use the session
	 * scope, make sure that Pet object always has an id (Even though id is not part of
	 * the form fields)
	 * @param appointmentId
	 * @return Appointment
	 */
	@ModelAttribute("appointment")
	public Appointment loadAppointmentWithPetAndVet(@PathVariable(name = "appointmentId", required = false) Integer appointmentId,
			@PathVariable("petId") int petId, ModelMap model) {
		Pet pet = this.pets.findById(petId);
		model.put("pet", pet);
		
		Owner owner = pet.getOwner();
		model.put("owner", owner);
		
		List<Vet> vetList = this.vets.findAll();
		model.put("vets", vetList);
		
		if (appointmentId == null) {
			Appointment appointment = new Appointment();
			appointment.setPet(pet);
			return appointment;
		}
		else {
			return this.appointments.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
		}
	}

	// Spring MVC calls method loadAppointmentWithPetAndVet(...) before
	// initNewAppointmentForm is called
	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String initNewAppointmentForm(@PathVariable("petId") int petId, ModelMap model) {
		return "appointments/createOrUpdateAppointmentForm";
	}

	// Spring MVC calls method loadAppointmentWithPetAndVet(...) before
	// processNewAppointmentForm is called
	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/new")
	public String processNewAppointmentForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			@Valid Appointment appointment, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "appointments/createOrUpdateAppointmentForm";
		}

		this.appointments.save(appointment);
		redirectAttributes.addFlashAttribute("message", "New appointment has been added");
		return "redirect:/owners/{ownerId}";
	}

	// Spring MVC calls method loadAppointmentWithPetAndVet(...) before
	// initUpdateAppointmentForm is called
	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments/{appointmentId}/edit")
	public String initUpdateAppointmentForm(@PathVariable("appointmentId") int appointmentId, ModelMap model) {
		return "appointments/createOrUpdateAppointmentForm";
	}

	// Spring MVC calls method loadAppointmentWithPetAndVet(...) before
	// processUpdateAppointmentForm is called
	@PostMapping("/owners/{ownerId}/pets/{petId}/appointments/{appointmentId}/edit")
	public String processUpdateAppointmentForm(@PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId, @PathVariable("appointmentId") int appointmentId,
			@Valid Appointment appointment, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "appointments/createOrUpdateAppointmentForm";
		}

		appointment.setId(appointmentId);
		this.appointments.save(appointment);
		redirectAttributes.addFlashAttribute("message", "Appointment has been updated");
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/appointments")
	public String showAppointments(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			ModelMap model) {
		Pet pet = this.pets.findById(petId);
		model.put("pet", pet);
		
		Owner owner = pet.getOwner();
		model.put("owner", owner);
		
		List<Appointment> appointmentList = this.appointments.findByPetId(petId);
		model.put("appointments", appointmentList);
		
		return "appointments/appointmentsList";
	}

}
