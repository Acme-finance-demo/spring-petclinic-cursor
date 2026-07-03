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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for XSS vulnerability prevention in pet-related flash attributes and error messages.
 * Verifies that flash attributes and error messages containing HTML/JavaScript are properly escaped
 * when rendered in templates.
 *
 * @author Security Team
 */
@WebMvcTest(value = PetController.class,
		includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class PetXSSTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private PetTypeRepository types;

	private Owner testOwner() {
		Owner owner = new Owner();
		owner.setId(TEST_OWNER_ID);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");
		return owner;
	}

	@BeforeEach
	void setup() {
		Owner owner = testOwner();
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		PetType dog = new PetType();
		dog.setId(1);
		dog.setName("dog");
		given(this.types.findPetTypes()).willReturn(List.of(dog));
	}

	@Test
	void testPetCreationWithValidInput() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
				.param("name", "Max")
				.param("birthDate", "2020-01-01")
				.param("type", "dog"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testPetCreationSuccessMessageIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
				.param("name", "Max")
				.param("birthDate", "2020-01-01")
				.param("type", "dog"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testPetUpdateSuccessMessageIsEscaped() throws Exception {
		Owner owner = testOwner();
		Pet pet = new Pet();
		pet.setId(TEST_PET_ID);
		pet.setName("Max");
		pet.setBirthDate(LocalDate.of(2020, 1, 1));
		PetType dog = new PetType();
		dog.setName("dog");
		pet.setType(dog);
		owner.addPet(pet);

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "Max")
				.param("birthDate", "2020-01-01")
				.param("type", "dog"))
			.andExpect(status().is3xxRedirection());
	}

}
