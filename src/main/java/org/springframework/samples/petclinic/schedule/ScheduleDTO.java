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
package org.springframework.samples.petclinic.schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for Schedule API requests and responses.
 *
 * @author Generated
 */
public class ScheduleDTO {

	private Integer id;

	@NotNull(message = "Veterinarian ID is required")
	private Integer vetId;

	@NotNull(message = "Day of week is required")
	private DayOfWeek dayOfWeek;

	@NotNull(message = "Start time is required")
	private LocalTime startTime;

	@NotNull(message = "End time is required")
	private LocalTime endTime;

	private Boolean isAvailable = true;

	// Constructors
	public ScheduleDTO() {
	}

	public ScheduleDTO(Integer vetId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
		this.vetId = vetId;
		this.dayOfWeek = dayOfWeek;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isAvailable = true;
	}

	// Getters and Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getVetId() {
		return vetId;
	}

	public void setVetId(Integer vetId) {
		this.vetId = vetId;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

}
