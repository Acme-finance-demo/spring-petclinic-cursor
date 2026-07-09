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
package org.springframework.samples.petclinic.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link WeatherController}
 */
@WebMvcTest(WeatherController.class)
class WeatherControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private WeatherService weatherService;

	@BeforeEach
	void setup() {
		WeatherResponse response = new WeatherResponse();
		response.setCity("Tokyo");
		response.setTimestamp("2025-01-15T10:30:00Z");

		WeatherCondition condition = new WeatherCondition();
		condition.setCondition("Cloudy");
		condition.setDescription("曇り");
		condition.setIcon("02d");
		response.setWeather(condition);

		Temperature temperature = new Temperature();
		temperature.setCurrent(15.5);
		temperature.setFeelsLike(14.2);
		temperature.setMin(12.0);
		temperature.setMax(18.5);
		temperature.setUnit("Celsius");
		response.setTemperature(temperature);

		response.setHumidity(65);
		response.setPressure(1013);
		response.setWindSpeed(3.5);
		response.setVisibility(10000);
		response.setCloudCoverage(75);
		response.setUvIndex(2);

		given(this.weatherService.getTokyoWeather()).willReturn(response);
	}

	@Test
	void testGetTokyoWeather() throws Exception {
		mockMvc.perform(get("/api/weather/tokyo").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.city").value("Tokyo"))
			.andExpect(jsonPath("$.weather.condition").value("Cloudy"))
			.andExpect(jsonPath("$.weather.description").value("曇り"))
			.andExpect(jsonPath("$.temperature.current").value(15.5))
			.andExpect(jsonPath("$.temperature.unit").value("Celsius"))
			.andExpect(jsonPath("$.humidity").value(65))
			.andExpect(jsonPath("$.pressure").value(1013))
			.andExpect(jsonPath("$.windSpeed").value(3.5))
			.andExpect(jsonPath("$.visibility").value(10000))
			.andExpect(jsonPath("$.cloudCoverage").value(75))
			.andExpect(jsonPath("$.uvIndex").value(2));
	}

}
