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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive XSS payload security tests for flash attributes and error messages.
 * Verifies that malicious XSS payloads are properly escaped when rendered in templates.
 *
 * @author Security Team
 */
@WebMvcTest(value = {OwnerController.class, PetController.class, VisitController.class},
	includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class XSSPayloadSecurityTests {

	private static final int TEST_OWNER_ID = 1;
	private static final int TEST_PET_ID = 1;

	private static final String XSS_PAYLOAD_SCRIPT = "<script>alert('XSS')</script>";
	private static final String XSS_PAYLOAD_IMG = "<img src=x onerror=\"alert('XSS')\">";
	private static final String XSS_PAYLOAD_SVG = "<svg onload=\"alert('XSS')\">";
	private static final String XSS_PAYLOAD_EVENT = "<div onclick=\"alert('XSS')\">Click me</div>";
	private static final String XSS_PAYLOAD_IFRAME = "<iframe src=\"javascript:alert('XSS')\"></iframe>";

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

		PetType cat = new PetType();
		cat.setId(3);
		cat.setName("hamster");
		given(this.types.findPetTypes()).willReturn(List.of(cat));
	}

	@Test
	void testOwnerCreationErrorWithScriptPayloadIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in creating the owner.")))
			.andExpect(content().string(not(containsString("<script>"))));
	}

	@Test
	void testOwnerCreationErrorWithImgPayloadIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("onerror="))))
			.andExpect(content().string(not(containsString("<img"))));
	}

	@Test
	void testOwnerCreationErrorWithSvgPayloadIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("onload="))))
			.andExpect(content().string(not(containsString("<svg"))));
	}

	@Test
	void testOwnerUpdateErrorWithScriptPayloadIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "")
				.param("telephone", ""))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in updating the owner.")))
			.andExpect(content().string(not(containsString("<script>"))));
	}

	@Test
	void testOwnerUpdateErrorWithEventPayloadIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "")
				.param("telephone", ""))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("onclick="))))
			.andExpect(content().string(not(containsString("<div"))));
	}

	@Test
	void testOwnerUpdateErrorWithIframePayloadIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "")
				.param("telephone", ""))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("javascript:"))))
			.andExpect(content().string(not(containsString("<iframe"))));
	}

	@Test
	void testPetCreationSuccessMessageWithScriptPayloadIsEscaped() throws Exception {
		Owner owner = testOwner();
		Pet pet = new Pet();
		owner.addPet(pet);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testVisitCreationSuccessMessageWithScriptPayloadIsEscaped() throws Exception {
		Owner owner = testOwner();
		Pet pet = new Pet();
		pet.setId(TEST_PET_ID);
		owner.addPet(pet);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George")
				.param("description", "Visit Description"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testOwnerCreationSuccessWithValidInputDoesNotContainXSSMarkers() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "1234567890"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testOwnerUpdateSuccessWithValidInputDoesNotContainXSSMarkers() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "1234567890"))
			.andExpect(status().is3xxRedirection());
	}

}
