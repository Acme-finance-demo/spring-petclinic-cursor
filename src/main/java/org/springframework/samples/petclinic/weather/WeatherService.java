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

import java.time.Instant;

import org.springframework.stereotype.Service;

/**
 * Service class for weather operations.
 */
@Service
public class WeatherService {

	public WeatherResponse getTokyoWeather() {
		WeatherResponse response = new WeatherResponse();
		response.setCity("Tokyo");
		response.setTimestamp(Instant.now().toString());

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

		return response;
	}

}
