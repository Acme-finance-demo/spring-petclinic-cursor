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

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.vet.Vet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * Simple JavaBean domain object representing a veterinarian's holiday/unavailable day.
 *
 * @author Spring Petclinic Team
 */
@Entity
@Table(name = "vet_holidays")
public class VetHoliday extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "vet_id")
	@NotNull
	private Vet vet;

	@Column(name = "holiday_date")
	@NotNull
	private LocalDate holidayDate;

	@Column(name = "reason")
	private String reason;

	public Vet getVet() {
		return vet;
	}

	public void setVet(Vet vet) {
		this.vet = vet;
	}

	public LocalDate getHolidayDate() {
		return holidayDate;
	}

	public void setHolidayDate(LocalDate holidayDate) {
		this.holidayDate = holidayDate;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Check if this holiday affects a given date
	 */
	public boolean affectsDate(LocalDate date) {
		return holidayDate != null && holidayDate.equals(date);
	}

	/**
	 * Check if this holiday is in the past
	 */
	public boolean isPast() {
		return holidayDate != null && holidayDate.isBefore(LocalDate.now());
	}

	/**
	 * Check if this holiday is today
	 */
	public boolean isToday() {
		return holidayDate != null && holidayDate.equals(LocalDate.now());
	}

	/**
	 * Check if this holiday is in the future
	 */
	public boolean isFuture() {
		return holidayDate != null && holidayDate.isAfter(LocalDate.now());
	}

}