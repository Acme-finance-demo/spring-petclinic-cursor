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

import java.util.ArrayList;
import java.util.List;

/**
 * Result object for appointment validation.
 *
 * @author Spring Petclinic Team
 */
public class AppointmentValidationResult {

	private final List<String> errors = new ArrayList<>();
	private final List<String> warnings = new ArrayList<>();

	/**
	 * Add an error message.
	 */
	public void addError(String error) {
		errors.add(error);
	}

	/**
	 * Add a warning message.
	 */
	public void addWarning(String warning) {
		warnings.add(warning);
	}

	/**
	 * Check if validation passed (no errors).
	 */
	public boolean isValid() {
		return errors.isEmpty();
	}

	/**
	 * Check if there are any errors.
	 */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	/**
	 * Check if there are any warnings.
	 */
	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	/**
	 * Get all error messages.
	 */
	public List<String> getErrors() {
		return new ArrayList<>(errors);
	}

	/**
	 * Get all warning messages.
	 */
	public List<String> getWarnings() {
		return new ArrayList<>(warnings);
	}

	/**
	 * Get combined error and warning messages.
	 */
	public List<String> getAllMessages() {
		List<String> allMessages = new ArrayList<>(errors);
		allMessages.addAll(warnings);
		return allMessages;
	}

	/**
	 * Get the first error message, if any.
	 */
	public String getFirstError() {
		return errors.isEmpty() ? null : errors.get(0);
	}

	/**
	 * Clear all errors and warnings.
	 */
	public void clear() {
		errors.clear();
		warnings.clear();
	}

}