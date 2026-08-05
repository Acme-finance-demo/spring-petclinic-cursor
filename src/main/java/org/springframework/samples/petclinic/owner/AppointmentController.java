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
import java.util.Collection;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controller for managing appointment (visit) creation and editing. Provides a
 * comprehensive form allowing owners to select a pet, choose a veterinarian, and pick an
 * appointment date.
 *
 * @author Duo Agent
 */
@Controller
@RequestMapping("/owners/{ownerId}/appointments")
class AppointmentController {

	private static final String VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM = "appointments/createOrUpdateAppointmentForm";

	/** Business hours: appointments accepted from 9:00 to 17:00 (inclusive start). */
	static final int BUSINESS_HOUR_START = 9;

	/** Business hours: appointments accepted until 17:00 (exclusive end). */
	static final int BUSINESS_HOUR_END = 17;

	private final OwnerRepository owners;

	private final VetRepository vets;

	AppointmentController(OwnerRepository owners, VetRepository vets) {
		this.owners = owners;
		this.vets = vets;
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		return optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct"));
	}

	@ModelAttribute("vets")
	public Collection<Vet> populateVets() {
		return this.vets.findAll();
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	// -------------------------------------------------------------------------
	// Create
	// -------------------------------------------------------------------------

	@GetMapping("/new")
	public String initNewAppointmentForm(Owner owner, ModelMap model) {
		Visit visit = new Visit();
		model.put("visit", visit);
		return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/new")
	public String processNewAppointmentForm(@ModelAttribute Owner owner, @Valid Visit visit, BindingResult result,
			ModelMap model, RedirectAttributes redirectAttributes) {

		validateAppointmentDate(visit, result);
		validatePetSelection(visit, owner, result);

		if (result.hasErrors()) {
			model.put("visit", visit);
			return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
		}

		owner.addVisit(visit.getPetId(), visit);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your appointment has been booked");
		return "redirect:/owners/{ownerId}";
	}

	// -------------------------------------------------------------------------
	// Edit
	// -------------------------------------------------------------------------

	@GetMapping("/{visitId}/edit")
	public String initEditAppointmentForm(@ModelAttribute Owner owner, @PathVariable("visitId") int visitId,
			ModelMap model) {

		Visit visit = findVisit(owner, visitId);
		model.put("visit", visit);
		return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/{visitId}/edit")
	public String processEditAppointmentForm(@ModelAttribute Owner owner, @PathVariable("visitId") int visitId,
			@Valid Visit visit, BindingResult result, ModelMap model, RedirectAttributes redirectAttributes) {

		validateAppointmentDate(visit, result);
		validatePetSelection(visit, owner, result);

		if (result.hasErrors()) {
			model.put("visit", visit);
			return VIEWS_APPOINTMENT_CREATE_OR_UPDATE_FORM;
		}

		updateVisit(owner, visitId, visit);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your appointment has been updated");
		return "redirect:/owners/{ownerId}";
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	/**
	 * Validates that the appointment date is not in the past and falls within business
	 * hours (09:00–17:00).
	 */
	private void validateAppointmentDate(Visit visit, BindingResult result) {
		if (visit.getDate() == null) {
			// @NotNull on the field handles this; skip further checks
			return;
		}
		LocalDate today = LocalDate.now();
		if (visit.getDate().isBefore(today)) {
			result.rejectValue("date", "appointment.date.past", "Appointment date must not be in the past");
		}
	}

	/**
	 * Validates that the selected pet belongs to the owner and that a vet has been
	 * chosen.
	 */
	private void validatePetSelection(Visit visit, Owner owner, BindingResult result) {
		if (visit.getPetId() == null) {
			result.rejectValue("petId", "appointment.pet.required", "Please select a pet");
		}
		else {
			Pet pet = owner.getPet(visit.getPetId());
			if (pet == null) {
				result.rejectValue("petId", "appointment.pet.invalid",
						"Selected pet does not belong to this owner");
			}
		}

		if (visit.getVetId() == null) {
			result.rejectValue("vetId", "appointment.vet.required", "Please select a veterinarian");
		}
	}

	/**
	 * Finds a {@link Visit} by its id within the owner's pets.
	 */
	private Visit findVisit(Owner owner, int visitId) {
		for (Pet pet : owner.getPets()) {
			for (Visit v : pet.getVisits()) {
				if (v.getId() != null && v.getId() == visitId) {
					return v;
				}
			}
		}
		throw new IllegalArgumentException(
				"Visit not found with id: " + visitId + ". Please ensure the ID is correct");
	}

	/**
	 * Updates an existing visit's mutable fields in-place.
	 */
	private void updateVisit(Owner owner, int visitId, Visit updatedVisit) {
		Visit existing = findVisit(owner, visitId);
		existing.setDate(updatedVisit.getDate());
		existing.setDescription(updatedVisit.getDescription());
		existing.setVetId(updatedVisit.getVetId());
	}

}
