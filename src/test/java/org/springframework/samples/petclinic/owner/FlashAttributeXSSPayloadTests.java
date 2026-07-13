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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security test class for XSS vulnerability prevention with actual XSS payloads in flash
 * attributes. Verifies that flash attributes containing HTML/JavaScript are properly
 * escaped when rendered in templates, preventing stored XSS attacks.
 *
 * @author Security Team
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class FlashAttributeXSSPayloadTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

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
	}

	@Test
	void testOwnerCreationWithXSSPayloadInFirstNameIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "<script>alert('XSS')</script>")
				.param("lastName", "Bloggs")
				.param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in creating the owner.")))
			.andExpect(content().string(not(containsString("<script>alert('XSS')</script>"))));
	}

	@Test
	void testOwnerCreationWithXSSPayloadInLastNameIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "<img src=x onerror=alert('XSS')>")
				.param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in creating the owner.")))
			.andExpect(content().string(not(containsString("<img src=x onerror=alert('XSS')>"))));
	}

	@Test
	void testOwnerCreationWithXSSPayloadInAddressIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "\"><script>alert('XSS')</script>")
				.param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in creating the owner.")))
			.andExpect(content().string(not(containsString("\"><script>alert('XSS')</script>"))));
	}

	@Test
	void testOwnerCreationWithXSSPayloadInCityIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Main St")
				.param("city", "<svg onload=alert('XSS')>")
				.param("telephone", "1234567890"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in creating the owner.")))
			.andExpect(content().string(not(containsString("<svg onload=alert('XSS')>"))));
	}

	@Test
	void testOwnerCreationWithXSSPayloadInTelephoneIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/new")
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Main St")
				.param("city", "London")
				.param("telephone", "123<script>alert('XSS')</script>456"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in creating the owner.")))
			.andExpect(content().string(not(containsString("<script>alert('XSS')</script>"))));
	}

	@Test
	void testOwnerUpdateWithXSSPayloadInFirstNameIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "<iframe src='javascript:alert(\"XSS\")'></iframe>")
				.param("lastName", "Bloggs")
				.param("address", "123 Main St")
				.param("city", "London")
				.param("telephone", "1234567890"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in updating the owner.")))
			.andExpect(content().string(not(containsString("<iframe src='javascript:alert(\"XSS\")'></iframe>"))));
	}

	@Test
	void testOwnerUpdateWithXSSPayloadInAddressIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Main St<body onload=alert('XSS')>")
				.param("city", "London")
				.param("telephone", "1234567890"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in updating the owner.")))
			.andExpect(content().string(not(containsString("<body onload=alert('XSS')>"))));
	}

	@Test
	void testOwnerUpdateWithXSSPayloadInCityIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Main St")
				.param("city", "London<marquee onstart=alert('XSS')>")
				.param("telephone", "1234567890"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in updating the owner.")))
			.andExpect(content().string(not(containsString("<marquee onstart=alert('XSS')>"))));
	}

	@Test
	void testOwnerUpdateWithXSSPayloadInTelephoneIsEscaped() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
				.param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Main St")
				.param("city", "London")
				.param("telephone", "123<input onfocus=alert('XSS') autofocus>456"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("There was an error in updating the owner.")))
			.andExpect(content().string(not(containsString("<input onfocus=alert('XSS') autofocus>"))));
	}

}
