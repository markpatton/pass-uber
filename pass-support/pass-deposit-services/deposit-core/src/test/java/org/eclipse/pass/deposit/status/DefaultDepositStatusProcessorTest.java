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

import static org.eclipse.pass.deposit.status.SwordDspaceDepositStatus.SWORD_STATE_ARCHIVED;
import static org.eclipse.pass.deposit.status.SwordDspaceDepositStatus.SWORD_STATE_INPROGRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.eclipse.pass.deposit.config.repository.BasicAuthRealm;
import org.eclipse.pass.deposit.config.repository.RepositoryConfig;
import org.eclipse.pass.deposit.config.repository.RepositoryDepositConfig;
import org.eclipse.pass.deposit.config.repository.StatusMapping;
import org.eclipse.pass.support.client.model.Deposit;
import org.eclipse.pass.support.client.model.DepositStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class DefaultDepositStatusProcessorTest {
    private DefaultDepositStatusProcessor underTest;

    private DepositStatusResolver<URI, URI> resolver;

    private StatusMapping mapping;

    private Deposit deposit;

    private String depositStatusRefBaseUrl = "http://example.org/";
    private String depositStatusRef = depositStatusRefBaseUrl + "statement.atom";

    private BasicAuthRealm authRealm;

    private RepositoryConfig repositoryConfig;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        resolver = mock(DepositStatusResolver.class);
        mapping = mock(StatusMapping.class);
        authRealm = mock(BasicAuthRealm.class);
        when(authRealm.getBaseUrl()).thenReturn(depositStatusRefBaseUrl);
        repositoryConfig = mock(RepositoryConfig.class);
        RepositoryDepositConfig depositConfig = mock(RepositoryDepositConfig.class);
        when(repositoryConfig.getRepositoryDepositConfig()).thenReturn(depositConfig);
        when(depositConfig.getStatusMapping()).thenReturn(mapping);

        underTest = new DefaultDepositStatusProcessor(resolver);
        deposit = mock(Deposit.class);
        when(deposit.getDepositStatusRef()).thenReturn(depositStatusRef);
    }

    @Test
    public void processingOk() {
        URI refUri = URI.create(depositStatusRef);
        when(resolver.resolve(refUri, repositoryConfig)).thenReturn(SWORD_STATE_ARCHIVED.asUri());
        when(mapping.getStatusMap()).thenReturn(
                Map.of(SWORD_STATE_ARCHIVED.asUri().toString(),
                        DepositStatus.ACCEPTED.name().toLowerCase()));

        assertEquals(DepositStatus.ACCEPTED,
                     underTest.process(deposit, repositoryConfig));

        verify(resolver).resolve(refUri, repositoryConfig);
        verify(mapping).getStatusMap();
    }

    @Test
    public void mappingReturnsUnknownDepositStatus() {
        String badDepositStatusUri = "http://foo/uri";

        URI refUri = URI.create(depositStatusRef);
        when(resolver.resolve(refUri, repositoryConfig)).thenReturn(SWORD_STATE_ARCHIVED.asUri());
        when(mapping.getStatusMap()).thenReturn(
                Map.of(SWORD_STATE_ARCHIVED.asUri().toString(), badDepositStatusUri));

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            underTest.process(deposit, repositoryConfig);
        });

        assertTrue(e.getMessage().contains(badDepositStatusUri));
        verify(resolver).resolve(refUri, repositoryConfig);
        verify(mapping).getStatusMap();
    }

    @Test
    public void parsingReturnsNullSwordStatus() {
        when(resolver.resolve(any(), eq(repositoryConfig))).thenReturn(null);

        assertNull(underTest.process(deposit, repositoryConfig));

        verify(resolver).resolve(any(), eq(repositoryConfig));
        verifyNoInteractions(mapping);
    }

    @Test
    public void mappingReturnsNullDepositStatus() {
        when(resolver.resolve(any(), eq(repositoryConfig))).thenReturn(SWORD_STATE_INPROGRESS.asUri());
        when(mapping.getStatusMap()).thenReturn(Collections.emptyMap());
        when(mapping.getDefaultMapping()).thenReturn(null);

        assertNull(underTest.process(deposit, repositoryConfig));

        verify(resolver).resolve(any(), eq(repositoryConfig));
        verify(mapping).getStatusMap();
        verify(mapping).getDefaultMapping();
    }

    @Test
    public void parsingThrowsRuntimeException() {
        when(resolver.resolve(any(), eq(repositoryConfig))).thenThrow(new RuntimeException("Expected"));

        Exception e = assertThrows(RuntimeException.class, () -> {
            underTest.process(deposit, repositoryConfig);
        });

        assertEquals("Expected", e.getMessage());
        verify(resolver).resolve(any(), eq(repositoryConfig));
        verifyNoInteractions(mapping);
    }

    @Test
    public void mappingThrowsRuntimeException() {
        when(resolver.resolve(any(), eq(repositoryConfig))).thenReturn(SWORD_STATE_INPROGRESS.asUri());
        when(mapping.getStatusMap()).thenThrow(new RuntimeException("Expected"));

        Exception e = assertThrows(RuntimeException.class, () -> {
            assertNull(underTest.process(deposit, repositoryConfig));
        });

        assertEquals("Expected", e.getMessage());
        verify(resolver).resolve(any(), eq(repositoryConfig));
    }
}