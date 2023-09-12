/*
 * Copyright 2022 Johns Hopkins University
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
package org.eclipse.pass.main;

import javax.persistence.OptimisticLockException;

import com.yahoo.elide.core.exceptions.CustomErrorException;
import com.yahoo.elide.core.exceptions.ErrorMapper;
import com.yahoo.elide.core.exceptions.ErrorObjects;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PassErrorMapper implements ErrorMapper {
    @Nullable
    @Override
    public CustomErrorException map(Exception e) {
        if (e instanceof OptimisticLockException) {
            ErrorObjects errors = ErrorObjects.builder().addError().withDetail(e.getMessage()).build();
            return new CustomErrorException(HttpStatus.CONFLICT.value(), e.getMessage(), errors);
        }
        return null;
    }
}
