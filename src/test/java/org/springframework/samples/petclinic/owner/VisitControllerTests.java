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
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(VisitController.class)
@DisabledInNativeImage
@DisabledInAotMode
class VisitControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	private static final int TEST_VISIT_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private VetRepository vets;

	@MockitoBean
	private VisitRepository visits;

	@BeforeEach
	void init() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
		given(this.vets.findAll()).willReturn(Collections.emptyList());

		Visit visit = new Visit();
		visit.setId(TEST_VISIT_ID);
		visit.setDescription("Test visit");
		visit.setVetId(1);
		visit.setDate(LocalDate.now().plusDays(1));
		given(this.visits.findById(TEST_VISIT_ID)).willReturn(Optional.of(visit));
	}

	@Test
	void testInitNewVisitForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void testProcessNewVisitFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George")
				.param("description", "Visit Description")
				.param("vetId", "1")
				.param("date", LocalDate.now().plusDays(1).toString())
				.param("appointmentTime", "10:00"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessNewVisitFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("name",
					"George"))
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

	@Test
	void testInitEditVisitForm() throws Exception {
		mockMvc
			.perform(get("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit", TEST_OWNER_ID, TEST_PET_ID,
					TEST_VISIT_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"))
			.andExpect(model().attributeExists("visit"));
	}

	@Test
	void testProcessEditVisitFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit", TEST_OWNER_ID, TEST_PET_ID,
					TEST_VISIT_ID)
				.param("description", "Updated Description")
				.param("vetId", "1")
				.param("date", LocalDate.now().plusDays(1).toString())
				.param("appointmentTime", "10:00"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessEditVisitFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/{visitId}/edit", TEST_OWNER_ID, TEST_PET_ID,
					TEST_VISIT_ID)
				.param("name", "George"))
			.andExpect(model().attributeHasErrors("visit"))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

}
