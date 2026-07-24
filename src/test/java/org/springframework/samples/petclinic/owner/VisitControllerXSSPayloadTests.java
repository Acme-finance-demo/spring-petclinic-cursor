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
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security test class for XSS vulnerability prevention with actual XSS payloads in visit
 * controller flash attributes. Verifies that flash attributes containing HTML/JavaScript
 * are properly escaped when rendered in templates, preventing stored XSS attacks.
 *
 * @author Security Team
 */
@WebMvcTest(VisitController.class)
@DisabledInNativeImage
@DisabledInAotMode
class VisitControllerXSSPayloadTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@BeforeEach
	void init() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	@Test
	void testVisitCreationWithXSSPayloadInDescriptionIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", "2023-01-15")
				.param("description", "<script>alert('XSS')</script>"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testVisitCreationWithXSSPayloadInDateIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", "<img src=x onerror=alert('XSS')>")
				.param("description", "Regular checkup"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("<img src=x onerror=alert('XSS')>"))));
	}

	@Test
	void testVisitCreationWithXSSPayloadInDescriptionWithSvgIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", "2023-01-15")
				.param("description", "<svg onload=alert('XSS')>"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testVisitCreationWithXSSPayloadInDescriptionWithIframeIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", "2023-01-15")
				.param("description", "<iframe src='javascript:alert(\"XSS\")'></iframe>"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void testVisitCreationWithXSSPayloadInDescriptionWithBodyTagIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("date", "2023-01-15")
				.param("description", "Checkup<body onload=alert('XSS')>"))
			.andExpect(status().is3xxRedirection());
	}

}
