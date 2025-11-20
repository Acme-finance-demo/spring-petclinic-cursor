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
package org.springframework.samples.petclinic.appointment;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.vet.Vet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Simple JavaBean domain object representing a veterinarian's schedule.
 *
 * @author Spring Petclinic Team
 */
@Entity
@Table(name = "vet_schedules")
public class VetSchedule extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "vet_id")
	@NotNull
	private Vet vet;

	@Column(name = "day_of_week")
	@NotNull
	@Min(0)
	@Max(6)
	private Integer dayOfWeek;

	@Column(name = "start_time")
	@NotNull
	private LocalTime startTime;

	@Column(name = "end_time")
	@NotNull
	private LocalTime endTime;

	@Column(name = "is_available")
	private Boolean isAvailable = true;

	public Vet getVet() {
		return vet;
	}

	public void setVet(Vet vet) {
		this.vet = vet;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * Set day of week using Java's DayOfWeek enum Note: DayOfWeek.MONDAY = 1, but our
	 * database uses 1 for Monday
	 */
	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		if (dayOfWeek == DayOfWeek.SUNDAY) {
			this.dayOfWeek = 0;
		}
		else {
			this.dayOfWeek = dayOfWeek.getValue();
		}
	}

	/**
	 * Get day of week as Java's DayOfWeek enum
	 */
	public DayOfWeek getDayOfWeekEnum() {
		if (dayOfWeek == null) {
			return null;
		}
		if (dayOfWeek == 0) {
			return DayOfWeek.SUNDAY;
		}
		return DayOfWeek.of(dayOfWeek);
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

	/**
	 * Get the day name for display purposes
	 */
	public String getDayName() {
		DayOfWeek dayEnum = getDayOfWeekEnum();
		return dayEnum != null ? dayEnum.name() : "";
	}

	/**
	 * Check if a given time falls within this schedule
	 */
	public boolean isTimeWithinSchedule(LocalTime time) {
		if (!Boolean.TRUE.equals(isAvailable) || startTime == null || endTime == null || time == null) {
			return false;
		}
		return !time.isBefore(startTime) && time.isBefore(endTime);
	}

	/**
	 * Get the duration of this schedule in minutes
	 */
	public long getDurationMinutes() {
		if (startTime == null || endTime == null) {
			return 0;
		}
		return java.time.Duration.between(startTime, endTime).toMinutes();
	}

}