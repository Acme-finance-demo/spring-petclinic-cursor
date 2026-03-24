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

/**
 * Simple JavaBean domain object representing a weather response.
 */
public class WeatherResponse {

	private String city;

	private String timestamp;

	private WeatherCondition weather;

	private Temperature temperature;

	private int humidity;

	private int pressure;

	private double windSpeed;

	private int visibility;

	private int cloudCoverage;

	private int uvIndex;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public WeatherCondition getWeather() {
		return weather;
	}

	public void setWeather(WeatherCondition weather) {
		this.weather = weather;
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public void setTemperature(Temperature temperature) {
		this.temperature = temperature;
	}

	public int getHumidity() {
		return humidity;
	}

	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}

	public int getPressure() {
		return pressure;
	}

	public void setPressure(int pressure) {
		this.pressure = pressure;
	}

	public double getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(double windSpeed) {
		this.windSpeed = windSpeed;
	}

	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public int getCloudCoverage() {
		return cloudCoverage;
	}

	public void setCloudCoverage(int cloudCoverage) {
		this.cloudCoverage = cloudCoverage;
	}

	public int getUvIndex() {
		return uvIndex;
	}

	public void setUvIndex(int uvIndex) {
		this.uvIndex = uvIndex;
	}

}
