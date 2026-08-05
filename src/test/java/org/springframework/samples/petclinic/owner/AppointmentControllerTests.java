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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link AppointmentController}.
 */
@WebMvcTest(AppointmentController.class)
@DisabledInNativeImage
@DisabledInAotMode
class AppointmentControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	private static final int TEST_VET_ID = 1;

	private static final int TEST_VISIT_ID = 10;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private VetRepository vets;

	@BeforeEach
	void setup() {
		// Set up owner with one pet that has one existing visit
		Owner owner = new Owner();
		owner.setId(TEST_OWNER_ID);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");

		Pet pet = new Pet();
		pet.setId(TEST_PET_ID);
		pet.setName("Leo");
		owner.addPet(pet);

		Visit existingVisit = new Visit();
		existingVisit.setId(TEST_VISIT_ID);
		existingVisit.setDate(LocalDate.now().plusDays(5));
		existingVisit.setDescription("Annual check-up");
		existingVisit.setVetId(TEST_VET_ID);
		pet.addVisit(existingVisit);

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		// Set up vet
		Vet vet = new Vet();
		vet.setId(TEST_VET_ID);
		vet.setFirstName("James");
		vet.setLastName("Carter");
		given(this.vets.findAll()).willReturn(List.of(vet));
	}

	// -------------------------------------------------------------------------
	// GET /new
	// -------------------------------------------------------------------------

	@Test
	void testInitNewAppointmentForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/appointments/new", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createOrUpdateAppointmentForm"))
			.andExpect(model().attributeExists("visit"))
			.andExpect(model().attributeExists("vets"));
	}

	// -------------------------------------------------------------------------
	// POST /new — success
	// -------------------------------------------------------------------------

	@Test
	void testProcessNewAppointmentFormSuccess() throws Exception {
		String futureDate = LocalDate.now().plusDays(1).toString();
		mockMvc
			.perform(post("/owners/{ownerId}/appointments/new", TEST_OWNER_ID).param("petId",
					String.valueOf(TEST_PET_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", futureDate)
				.param("description", "Routine check-up"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	// -------------------------------------------------------------------------
	// POST /new — validation errors
	// -------------------------------------------------------------------------

	@Test
	void testProcessNewAppointmentFormMissingPet() throws Exception {
		String futureDate = LocalDate.now().plusDays(1).toString();
		mockMvc
			.perform(post("/owners/{ownerId}/appointments/new", TEST_OWNER_ID)
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", futureDate)
				.param("description", "Routine check-up"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createOrUpdateAppointmentForm"));
	}

	@Test
	void testProcessNewAppointmentFormMissingVet() throws Exception {
		String futureDate = LocalDate.now().plusDays(1).toString();
		mockMvc
			.perform(post("/owners/{ownerId}/appointments/new", TEST_OWNER_ID)
				.param("petId", String.valueOf(TEST_PET_ID))
				.param("date", futureDate)
				.param("description", "Routine check-up"))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createOrUpdateAppointmentForm"));
	}

	@Test
	void testProcessNewAppointmentFormMissingDescription() throws Exception {
		String futureDate = LocalDate.now().plusDays(1).toString();
		mockMvc
			.perform(post("/owners/{ownerId}/appointments/new", TEST_OWNER_ID)
				.param("petId", String.valueOf(TEST_PET_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", futureDate))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(model().attributeHasFieldErrors("visit", "description"))
			.andExpect(view().name("appointments/createOrUpdateAppointmentForm"));
	}

	@Test
	void testProcessNewAppointmentFormPastDate() throws Exception {
		String pastDate = LocalDate.now().minusDays(1).toString();
		mockMvc
			.perform(post("/owners/{ownerId}/appointments/new", TEST_OWNER_ID)
				.param("petId", String.valueOf(TEST_PET_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", pastDate)
				.param("description", "Routine check-up"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(model().attributeHasFieldErrors("visit", "date"))
			.andExpect(view().name("appointments/createOrUpdateAppointmentForm"));
	}

	// -------------------------------------------------------------------------
	// GET /{visitId}/edit
	// -------------------------------------------------------------------------

	@Test
	void testInitEditAppointmentForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/appointments/{visitId}/edit", TEST_OWNER_ID, TEST_VISIT_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("appointments/createOrUpdateAppointmentForm"))
			.andExpect(model().attributeExists("visit"));
	}

	@Test
	void testInitEditAppointmentFormWithInvalidVisitId() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/appointments/{visitId}/edit", TEST_OWNER_ID, 999))
			.andExpect(status().is5xxServerError());
	}

	// -------------------------------------------------------------------------
	// POST /{visitId}/edit — success
	// -------------------------------------------------------------------------

	@Test
	void testProcessEditAppointmentFormSuccess() throws Exception {
		String futureDate = LocalDate.now().plusDays(3).toString();
		mockMvc
			.perform(post("/owners/{ownerId}/appointments/{visitId}/edit", TEST_OWNER_ID, TEST_VISIT_ID)
				.param("petId", String.valueOf(TEST_PET_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", futureDate)
				.param("description", "Updated check-up"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	// -------------------------------------------------------------------------
	// POST /{visitId}/edit — validation errors
	// -------------------------------------------------------------------------

	@Test
	void testProcessEditAppointmentFormPastDate() throws Exception {
		String pastDate = LocalDate.now().minusDays(1).toString();
		mockMvc
			.perform(post("/owners/{ownerId}/appointments/{visitId}/edit", TEST_OWNER_ID, TEST_VISIT_ID)
				.param("petId", String.valueOf(TEST_PET_ID))
				.param("vetId", String.valueOf(TEST_VET_ID))
				.param("date", pastDate)
				.param("description", "Updated check-up"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(model().attributeHasFieldErrors("visit", "date"))
			.andExpect(view().name("appointments/createOrUpdateAppointmentForm"));
	}

}
