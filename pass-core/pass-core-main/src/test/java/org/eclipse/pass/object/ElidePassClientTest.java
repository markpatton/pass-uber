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
package org.eclipse.pass.object;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.model.AggregatedDepositStatus;
import org.eclipse.pass.object.model.Deposit;
import org.eclipse.pass.object.model.DepositStatus;
import org.eclipse.pass.object.model.Source;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.SubmissionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

public class ElidePassClientTest extends PassClientTest {
    @Autowired
    protected RefreshableElide refreshableElide;

    @Override
    protected PassClient getNewClient() {
        return new ElidePassClient(refreshableElide);
    }

    @Test
    public void testUpdateSubmission_OptimisticLocking() throws IOException {
        Submission submission = new Submission();
        submission.setAggregatedDepositStatus(AggregatedDepositStatus.NOT_STARTED);
        submission.setSubmissionStatus(SubmissionStatus.DRAFT);
        submission.setSubmitterName("Bessie");
        client.createObject(submission);

        Submission updateSub1 = client.getObject(submission.getClass(), submission.getId());
        Submission updateSub2 = client.getObject(submission.getClass(), submission.getId());

        updateSub1.setSource(Source.OTHER);
        updateSub1.setSubmissionStatus(SubmissionStatus.SUBMITTED);

        client.updateObject(updateSub1);

        IOException ioException = assertThrows(IOException.class, () -> {
            updateSub2.setSource(null);
            updateSub2.setSubmissionStatus(SubmissionStatus.CHANGES_REQUESTED);
            client.updateObject(updateSub2);
        });

        assertTrue(ioException.getMessage().startsWith("Failed to update object: 409 " +
            "{\"errors\":[{\"detail\":\"Optimistic lock check failed for Submission [ID="));
        assertTrue(ioException.getMessage().endsWith("]. Request version: 0, Stored version: 1\"}]}"));
    }

    @Test
    public void testUpdateSubmission_OptimisticLocking_NullVersion() throws IOException {
        Submission submission = new Submission();
        submission.setAggregatedDepositStatus(AggregatedDepositStatus.NOT_STARTED);
        submission.setSubmissionStatus(SubmissionStatus.DRAFT);
        submission.setSubmitterName("Bessie");
        client.createObject(submission);

        Submission updateSub1 = client.getObject(submission.getClass(), submission.getId());
        Submission updateSub2 = client.getObject(submission.getClass(), submission.getId());

        updateSub1.setSource(Source.OTHER);
        updateSub1.setSubmissionStatus(SubmissionStatus.SUBMITTED);

        client.updateObject(updateSub1);

        IOException ioException = assertThrows(IOException.class, () -> {
            updateSub2.setSource(null);
            updateSub2.setSubmissionStatus(SubmissionStatus.CHANGES_REQUESTED);
            ReflectionTestUtils.setField(updateSub2, "version", null);
            client.updateObject(updateSub2);
        });

        assertTrue(ioException.getMessage().startsWith("Failed to update object: 409 " +
            "{\"errors\":[{\"detail\":\"Optimistic lock check failed for Submission [ID="));
        assertTrue(ioException.getMessage().endsWith("]. Request version: null, Stored version: 1\"}]}"));
    }

    @Test
    public void testUpdateDeposit_OptimisticLocking() throws IOException {
        Deposit deposit = new Deposit();
        deposit.setDepositStatus(DepositStatus.SUBMITTED);
        client.createObject(deposit);

        Deposit updateDep1 = client.getObject(deposit.getClass(), deposit.getId());
        Deposit updateDep2 = client.getObject(deposit.getClass(), deposit.getId());

        updateDep1.setDepositStatus(DepositStatus.FAILED);
        client.updateObject(updateDep1);

        IOException ioException = assertThrows(IOException.class, () -> {
            updateDep2.setDepositStatus(DepositStatus.ACCEPTED);
            client.updateObject(updateDep2);
        });

        assertTrue(ioException.getMessage().startsWith("Failed to update object: 409 " +
            "{\"errors\":[{\"detail\":\"Optimistic lock check failed for Deposit [ID="));
        assertTrue(ioException.getMessage().endsWith("]. Request version: 0, Stored version: 1\"}]}"));
    }

    @Test
    public void testUpdateDeposit_OptimisticLocking_NullVersion() throws IOException {
        Deposit deposit = new Deposit();
        deposit.setDepositStatus(DepositStatus.SUBMITTED);
        client.createObject(deposit);

        Deposit updateDep1 = client.getObject(deposit.getClass(), deposit.getId());
        Deposit updateDep2 = client.getObject(deposit.getClass(), deposit.getId());

        updateDep1.setDepositStatus(DepositStatus.FAILED);
        client.updateObject(updateDep1);

        IOException ioException = assertThrows(IOException.class, () -> {
            updateDep2.setDepositStatus(DepositStatus.ACCEPTED);
            ReflectionTestUtils.setField(updateDep2, "version", null);
            client.updateObject(updateDep2);
        });

        assertTrue(ioException.getMessage().startsWith("Failed to update object: 409 " +
            "{\"errors\":[{\"detail\":\"Optimistic lock check failed for Deposit [ID="));
        assertTrue(ioException.getMessage().endsWith("]. Request version: null, Stored version: 1\"}]}"));
    }
}