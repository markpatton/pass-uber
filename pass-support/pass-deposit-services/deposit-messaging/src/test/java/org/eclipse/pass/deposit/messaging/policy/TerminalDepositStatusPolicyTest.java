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
package org.eclipse.pass.deposit.messaging.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.pass.deposit.messaging.status.StatusEvaluator;
import org.eclipse.pass.support.client.model.DepositStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class TerminalDepositStatusPolicyTest {

    private StatusEvaluator<DepositStatus> evaluator;

    private TerminalDepositStatusPolicy underTest;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        evaluator = mock(StatusEvaluator.class);
        underTest = new TerminalDepositStatusPolicy(evaluator);
    }

    @Test
    public void testNullStatus() {
        assertFalse(underTest.test(null));
        verifyNoInteractions(evaluator);
    }

    @Test
    public void testTerminalStatus() {
        DepositStatus terminal = DepositStatus.ACCEPTED;
        when(evaluator.isTerminal(terminal)).thenReturn(true);

        assertTrue(underTest.test(terminal));
        verify(evaluator).isTerminal(terminal);
    }

    @Test
    public void testIntermediateStatus() {
        DepositStatus terminal = DepositStatus.SUBMITTED;
        when(evaluator.isTerminal(terminal)).thenReturn(false);

        assertFalse(underTest.test(terminal));
        verify(evaluator).isTerminal(terminal);
    }

}