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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for XSS vulnerability prevention in validation error messages. Verifies that
 * validation error messages are properly escaped when rendered in templates.
 *
 * @author Security Team
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class ValidationErrorXSSTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@BeforeEach
	void setup() {
	}

	@Test
	void testFindOwnersFormDisplaysWithoutXSS() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("<script>"))));
	}

	@Test
	void testValidationErrorsAreEscaped() throws Exception {
		mockMvc.perform(get("/owners?page=1").param("lastName", "TestUser")).andExpect(status().isOk());
	}

	@Test
	void testValidationErrorsWithXSSPayloadAreEscaped() throws Exception {
		mockMvc.perform(get("/owners?page=1").param("lastName", "<script>alert('XSS')</script>"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("<script>alert('XSS')</script>"))))
			.andExpect(content().string(containsString("&lt;script&gt;")));
	}

	@Test
	void testValidationErrorsWithHTMLTagsAreEscaped() throws Exception {
		mockMvc.perform(get("/owners?page=1").param("lastName", "<img src=x onerror=alert('XSS')>"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("<img src=x onerror=alert('XSS')>"))))
			.andExpect(content().string(containsString("&lt;img")));
	}

}
