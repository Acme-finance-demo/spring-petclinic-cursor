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

import java.util.Collection;
import java.util.Optional;

import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository class for <code>Pet</code> domain objects.
 *
 * @author Spring Petclinic Team
 */
public interface PetRepository extends JpaRepository<Pet, Integer> {

	/**
	 * Retrieve a {@link Pet} from the data store by id.
	 */
	Optional<Pet> findById(@Nonnull Integer id);

	/**
	 * Retrieve all {@link Pet}s from the data store.
	 */
	@Query("SELECT p FROM Pet p ORDER BY p.name")
	Collection<Pet> findAll();

	/**
	 * Retrieve {@link Pet}s by owner id.
	 */
	@Query("SELECT p FROM Pet p WHERE p.owner.id = :ownerId ORDER BY p.name")
	Collection<Pet> findByOwnerId(@Param("ownerId") Integer ownerId);

	/**
	 * Retrieve {@link Pet}s by name (partial match).
	 */
	@Query("SELECT p FROM Pet p WHERE p.name LIKE %:name% ORDER BY p.name")
	Collection<Pet> findByNameContaining(@Param("name") String name);

}