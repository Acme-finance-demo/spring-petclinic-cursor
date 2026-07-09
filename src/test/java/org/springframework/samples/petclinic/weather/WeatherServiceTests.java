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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for {@link WeatherService}
 */
class WeatherServiceTests {

	@Test
	void testGetTokyoWeather() {
		WeatherService weatherService = new WeatherService();
		WeatherResponse response = weatherService.getTokyoWeather();

		assertThat(response).isNotNull();
		assertThat(response.getCity()).isEqualTo("Tokyo");
		assertThat(response.getTimestamp()).isNotNull();

		assertThat(response.getWeather()).isNotNull();
		assertThat(response.getWeather().getCondition()).isEqualTo("Cloudy");
		assertThat(response.getWeather().getDescription()).isEqualTo("曇り");
		assertThat(response.getWeather().getIcon()).isEqualTo("02d");

		assertThat(response.getTemperature()).isNotNull();
		assertThat(response.getTemperature().getCurrent()).isEqualTo(15.5);
		assertThat(response.getTemperature().getFeelsLike()).isEqualTo(14.2);
		assertThat(response.getTemperature().getMin()).isEqualTo(12.0);
		assertThat(response.getTemperature().getMax()).isEqualTo(18.5);
		assertThat(response.getTemperature().getUnit()).isEqualTo("Celsius");

		assertThat(response.getHumidity()).isEqualTo(65);
		assertThat(response.getPressure()).isEqualTo(1013);
		assertThat(response.getWindSpeed()).isEqualTo(3.5);
		assertThat(response.getVisibility()).isEqualTo(10000);
		assertThat(response.getCloudCoverage()).isEqualTo(75);
		assertThat(response.getUvIndex()).isEqualTo(2);
	}

}
