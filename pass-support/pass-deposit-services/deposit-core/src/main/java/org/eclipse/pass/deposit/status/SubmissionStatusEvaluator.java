/*
 * Copyright 2018 Johns Hopkins University
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
package org.eclipse.pass.deposit.status;

import java.util.Objects;

import org.eclipse.pass.support.client.model.AggregatedDepositStatus;
import org.springframework.stereotype.Component;

/**
 * Determines if a PASS {@link AggregatedDepositStatus} is <em>terminal</em>
 * or not.
 * <p>
 * <strong>N.B.</strong> {@code null} is <em>not</em> considered a status to be evaluated
 * </p>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
@Component
public class SubmissionStatusEvaluator implements StatusEvaluator<AggregatedDepositStatus> {

    /**
     * Determine if {@code status} is in a <em>terminal</em> state, {@link AggregatedDepositStatus#ACCEPTED}
     * or {@link AggregatedDepositStatus#REJECTED}.  A null status is not Terminal.
     *
     * @param status the status the PASS {@code DepositStatus}
     * @return {@code true} if the status is terminal
     */
    @Override
    public boolean isTerminal(AggregatedDepositStatus status) {
        if (Objects.isNull(status)) {
            return false;
        }
        return switch (status) {
            case ACCEPTED, REJECTED -> true;
            default -> false;
        };
    }
}
