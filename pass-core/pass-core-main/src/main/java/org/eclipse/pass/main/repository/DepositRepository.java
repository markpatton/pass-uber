/*
 * Copyright 2023 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.pass.main.repository;

import org.eclipse.pass.object.model.Deposit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Russ Poetker (rpoetke1@jh.edu)
 */
public interface DepositRepository extends CrudRepository<Deposit, String> {

    /**
     * Returns deposit version from datastore.
     * @param depositId the id of the deposit
     * @return the deposit version
     */
    @Query("select d.version from Deposit d where d.id = ?1")
    Long findDepositVersionById(Long depositId);
}
