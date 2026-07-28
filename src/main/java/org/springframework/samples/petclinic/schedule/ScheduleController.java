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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST Controller for managing veterinarian schedules.
 *
 * @author Generated
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

	private final ScheduleService scheduleService;

	public ScheduleController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	/**
	 * Get all schedules for a veterinarian.
	 *
	 * @param vetId the veterinarian ID
	 * @return list of schedules
	 */
	@GetMapping
	public ResponseEntity<List<ScheduleDTO>> getSchedulesByVet(@RequestParam Integer vetId) {
		List<Schedule> schedules = scheduleService.findSchedulesByVetId(vetId);
		List<ScheduleDTO> dtos = schedules.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}

	/**
	 * Get schedules for a veterinarian on a specific day.
	 *
	 * @param vetId the veterinarian ID
	 * @param dayOfWeek the day of week
	 * @return list of schedules
	 */
	@GetMapping("/by-day")
	public ResponseEntity<List<ScheduleDTO>> getSchedulesByVetAndDay(
			@RequestParam Integer vetId,
			@RequestParam DayOfWeek dayOfWeek) {
		List<Schedule> schedules = scheduleService.findSchedulesByVetIdAndDay(vetId, dayOfWeek);
		List<ScheduleDTO> dtos = schedules.stream()
			.map(this::convertToDTO)
			.collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}

	/**
	 * Get a schedule by ID.
	 *
	 * @param scheduleId the schedule ID
	 * @return the schedule
	 */
	@GetMapping("/{scheduleId}")
	public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable Integer scheduleId) {
		return scheduleService.findScheduleById(scheduleId)
			.map(schedule -> ResponseEntity.ok(convertToDTO(schedule)))
			.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Create a new schedule.
	 *
	 * @param scheduleDTO the schedule DTO
	 * @return the created schedule
	 */
	@PostMapping
	public ResponseEntity<ScheduleDTO> createSchedule(@Valid @RequestBody ScheduleDTO scheduleDTO) {
		// TODO: Implement schedule creation logic
		// - Validate vet exists
		// - Check for scheduling conflicts
		// - Validate time ranges
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Update an existing schedule.
	 *
	 * @param scheduleId the schedule ID
	 * @param scheduleDTO the schedule DTO
	 * @return the updated schedule
	 */
	@PutMapping("/{scheduleId}")
	public ResponseEntity<ScheduleDTO> updateSchedule(
			@PathVariable Integer scheduleId,
			@Valid @RequestBody ScheduleDTO scheduleDTO) {
		// TODO: Implement schedule update logic
		return ResponseEntity.ok().build();
	}

	/**
	 * Delete a schedule.
	 *
	 * @param scheduleId the schedule ID
	 * @return no content
	 */
	@DeleteMapping("/{scheduleId}")
	public ResponseEntity<Void> deleteSchedule(@PathVariable Integer scheduleId) {
		scheduleService.deleteSchedule(scheduleId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Convert Schedule entity to DTO.
	 *
	 * @param schedule the schedule entity
	 * @return the schedule DTO
	 */
	private ScheduleDTO convertToDTO(Schedule schedule) {
		ScheduleDTO dto = new ScheduleDTO();
		dto.setId(schedule.getId());
		dto.setVetId(schedule.getVet().getId());
		dto.setDayOfWeek(schedule.getDayOfWeek());
		dto.setStartTime(schedule.getStartTime());
		dto.setEndTime(schedule.getEndTime());
		dto.setIsAvailable(schedule.getIsAvailable());
		return dto;
	}

}
