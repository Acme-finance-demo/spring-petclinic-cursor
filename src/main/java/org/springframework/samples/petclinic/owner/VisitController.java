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
import java.util.Map;
import java.util.Optional;

import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 */
@Controller
class VisitController {

	private static final String VIEWS_VISIT_CREATE_OR_UPDATE_FORM = "pets/createOrUpdateVisitForm";

	private final OwnerRepository owners;

	private final VetRepository vets;

	private final VisitRepository visits;

	public VisitController(OwnerRepository owners, VetRepository vets, VisitRepository visits) {
		this.owners = owners;
		this.vets = vets;
		this.visits = visits;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Populates the list of vets for the visit form dropdown.
	 * @return a Collection of all {@link Vet}s
	 */
	@ModelAttribute("vets")
	public Collection<Vet> populateVets() {
		return this.vets.findAll();
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		model.put("owner", owner);

		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm() {
		return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (visit.getDate() != null && visit.getDate().isBefore(LocalDate.now())) {
			result.rejectValue("date", "pastDate", "Appointment date must not be in the past");
		}
		if (result.hasErrors()) {
			return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your visit has been booked");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Loads an existing visit for editing. The @ModelAttribute("visit") method runs first
	 * and populates pet/owner in the model; this method then overrides the visit with the
	 * existing one.
	 * @param visitId the visit ID
	 * @param model the model map
	 * @return the view name
	 */
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String initEditVisitForm(@PathVariable("visitId") int visitId, Map<String, Object> model) {
		Visit visit = this.visits.findById(visitId)
			.orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + visitId));
		model.put("visit", visit);
		return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes the edit visit form submission.
	 * @param owner the owner
	 * @param petId the pet ID
	 * @param visitId the visit ID
	 * @param visit the updated visit data
	 * @param result the binding result
	 * @param redirectAttributes redirect attributes
	 * @return redirect URL
	 */
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit")
	public String processEditVisitForm(@ModelAttribute Owner owner, @PathVariable int petId,
			@PathVariable int visitId, @Valid Visit visit, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (visit.getDate() != null && visit.getDate().isBefore(LocalDate.now())) {
			result.rejectValue("date", "pastDate", "Appointment date must not be in the past");
		}
		if (result.hasErrors()) {
			return VIEWS_VISIT_CREATE_OR_UPDATE_FORM;
		}

		visit.setId(visitId);
		this.visits.save(visit);
		redirectAttributes.addFlashAttribute("message", "Your visit has been updated");
		return "redirect:/owners/{ownerId}";
	}

}
