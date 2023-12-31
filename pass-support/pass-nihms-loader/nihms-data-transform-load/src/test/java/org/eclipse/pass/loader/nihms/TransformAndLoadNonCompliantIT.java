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
package org.eclipse.pass.loader.nihms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.pass.loader.nihms.model.NihmsPublication;
import org.eclipse.pass.loader.nihms.model.NihmsStatus;
import org.eclipse.pass.loader.nihms.util.ConfigUtil;
import org.eclipse.pass.support.client.PassClientSelector;
import org.eclipse.pass.support.client.RSQL;
import org.eclipse.pass.support.client.model.CopyStatus;
import org.eclipse.pass.support.client.model.Deposit;
import org.eclipse.pass.support.client.model.DepositStatus;
import org.eclipse.pass.support.client.model.Grant;
import org.eclipse.pass.support.client.model.Publication;
import org.eclipse.pass.support.client.model.Repository;
import org.eclipse.pass.support.client.model.RepositoryCopy;
import org.eclipse.pass.support.client.model.Source;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionStatus;
import org.eclipse.pass.support.client.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Karen Hanson
 */
@ExtendWith(MockitoExtension.class)
public class TransformAndLoadNonCompliantIT extends NihmsSubmissionEtlITBase {

    private final String pmid1 = "9999999999";
    private final String grant1 = "R01 AB123456";
    private final String nihmsId1 = "NIHMS987654321";
    private final String title = "Article A";
    private final String doi = "10.1000/a.abcd.1234";
    private final String issue = "3";

    /**
     * Tests when the publication is completely new and is non-compliant
     * publication, submission but no RepositoryCopy is created
     *
     * @throws Exception if an error occurs
     */

    @Test
    public void testNewNonCompliantPublication() throws Exception {
        PassClientSelector<Publication> pubSelector = new PassClientSelector<>(Publication.class);
        PassClientSelector<Submission> subSelector = new PassClientSelector<>(Submission.class);
        PassClientSelector<RepositoryCopy> repoCopySelector = new PassClientSelector<>(RepositoryCopy.class);
        User user = new User();
        passClient.createObject(user);
        String grantId = createGrant(grant1, user);

        setMockPMRecord(pmid1);

        //we should start with no publication for this pmid
        pubSelector.setFilter(RSQL.equals("pmid", pmid1));
        Optional<Publication> testPub = passClient.streamObjects(pubSelector).findAny();
        assertFalse(testPub.isPresent());

        //load all new publication, repo copy and submission
        NihmsPublication pub = newNonCompliantNihmsPub();
        NihmsTransformLoadService transformLoadService = new NihmsTransformLoadService(nihmsPassClientService,
                                                                                       mockPmidLookup, statusService);
        transformLoadService.transformAndLoadNihmsPub(pub);

        //wait for new publication to appear
        PassClientSelector<Publication> testPubSelector = new PassClientSelector<>(Publication.class);
        testPubSelector.setFilter(RSQL.equals("pmid", pmid1));
        Optional<Publication> testNewPub = passClient.streamObjects(testPubSelector).findFirst();
        assertTrue(testNewPub.isPresent());

        Publication publication = passClient.getObject(Publication.class, testNewPub.orElseThrow().getId());
        //spot check publication fields
        assertEquals(doi, publication.getDoi());
        assertEquals(title, publication.getTitle());
        assertEquals(issue, publication.getIssue());

        //now make sure we wait for submission, should only be one from the test
        subSelector.setFilter(RSQL.equals("publication.id", testNewPub.orElseThrow().getId()));
        List<Submission> testSub = passClient.streamObjects(subSelector).toList();
        assertEquals(1, testSub.size());

        Submission submission = passClient.getObject(Submission.class, testSub.get(0).getId());
        //check fields in submission
        assertEquals(grantId, submission.getGrants().get(0).getId());
        assertEquals(ConfigUtil.getNihmsRepositoryId(), submission.getRepositories().get(0).getId());
        assertEquals(1, submission.getRepositories().size());
        assertEquals(Source.OTHER, submission.getSource());
        assertFalse(submission.getSubmitted());
        assertEquals(user.getId(), submission.getSubmitter().getId());
        assertNull(submission.getSubmittedDate());
        assertEquals(SubmissionStatus.MANUSCRIPT_REQUIRED, submission.getSubmissionStatus());

        repoCopySelector.setFilter(RSQL.equals("publication.id", testNewPub.orElseThrow().getId()));
        Optional<RepositoryCopy> repoCopy = passClient.streamObjects(repoCopySelector).findAny();
        assertFalse(repoCopy.isPresent());
    }

    /**
     * Submission existed for repository/grant/user and there is a Deposit. Publication is now non-compliant.
     * This should create a repoCopy with STALLED status and associate it with Deposit
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testStalledPublicationExistingSubmissionAndDeposit() throws Exception {
        PassClientSelector<Publication> pubSelector = new PassClientSelector<>(Publication.class);
        PassClientSelector<RepositoryCopy> repoCopySelector = new PassClientSelector<>(RepositoryCopy.class);
        User user = new User();
        passClient.createObject(user);
        String grantId = createGrant(grant1, user);

        //we should start with no publication for this pmid
        pubSelector.setFilter(RSQL.equals("pmid", pmid1));
        Optional<Publication> testPub = passClient.streamObjects(pubSelector).findAny();
        assertFalse(testPub.isPresent());

        //create existing publication
        Publication publication = newPublication();
        passClient.createObject(publication);

        //a submission existed but had no repocopy
        Submission preexistingSub = newSubmission1(grantId, publication.getId(), user,true,
                SubmissionStatus.SUBMITTED);
        preexistingSub.setSubmitted(true);
        preexistingSub.setSource(Source.PASS);
        passClient.createObject(preexistingSub);

        PassClientSelector<Repository> nihmsRepoSel = new PassClientSelector<>(Repository.class);
        nihmsRepoSel.setFilter(RSQL.equals("id", ConfigUtil.getNihmsRepositoryId()));
        Repository nihmsRepo = passClient.streamObjects(nihmsRepoSel).findAny().orElseThrow();

        Deposit preexistingDeposit = new Deposit();
        preexistingDeposit.setDepositStatus(DepositStatus.SUBMITTED);
        preexistingDeposit.setRepository(nihmsRepo);
        preexistingDeposit.setSubmission(preexistingSub);
        passClient.createObject(preexistingDeposit);

        //now we have an existing publication, deposit, and submission for same grant/repo...
        //do transform/load to make sure we get a stalled repocopy and the deposit record is updated
        NihmsPublication pub = newNonCompliantStalledNihmsPub();
        NihmsTransformLoadService transformLoadService = new NihmsTransformLoadService(nihmsPassClientService,
                                                                                       mockPmidLookup, statusService);
        transformLoadService.transformAndLoadNihmsPub(pub);

        //make sure we wait for submission, should only be one from the test
        repoCopySelector.setFilter(RSQL.hasMember("externalIds", pub.getNihmsId()));
        Optional<RepositoryCopy> testRepoCopy = passClient.streamObjects(repoCopySelector).findAny();
        assertTrue(testRepoCopy.isPresent());

        Submission reloadedPreexistingSub = nihmsPassClientService.readSubmission(preexistingSub.getId());
        preexistingSub.setSubmissionStatus(SubmissionStatus.NEEDS_ATTENTION);
        verifySubmission(reloadedPreexistingSub, preexistingSub); //should not have been affected

        //we should have ONLY ONE submission for this pmid
        PassClientSelector<Submission> subSelOne = new PassClientSelector<>(Submission.class);
        subSelOne.setFilter(RSQL.equals("publication.id", publication.getId()));
        assertEquals(1, passClient.streamObjects(subSelOne).toList().size());

        //we should have ONLY ONE publication for this pmid
        PassClientSelector<Publication> pubSelOne = new PassClientSelector<>(Publication.class);
        pubSelOne.setFilter(RSQL.equals("pmid", pmid1));
        assertEquals(1, passClient.streamObjects(pubSelOne).toList().size());

        //we should have ONLY ONE repoCopy for this publication
        PassClientSelector<RepositoryCopy> repoCopySelOne = new PassClientSelector<>(RepositoryCopy.class);
        repoCopySelOne.setFilter(RSQL.equals("publication.id", publication.getId()));
        assertEquals(1, passClient.streamObjects(repoCopySelOne).toList().size());

        //validate the new repo copy
        RepositoryCopy repoCopy = passClient.getObject(RepositoryCopy.class, testRepoCopy.orElseThrow().getId());
        validateRepositoryCopy(repoCopy);
        assertEquals(CopyStatus.STALLED, repoCopy.getCopyStatus());

        //check repository copy link added, but status did not change... status managed by deposit service
        Deposit deposit = passClient.getObject(Deposit.class, preexistingDeposit.getId());
        assertEquals(DepositStatus.ACCEPTED, deposit.getDepositStatus());
        assertEquals(testRepoCopy.orElseThrow().getId(), deposit.getRepositoryCopy().getId());
    }

    /**
     * Submission exists for publication/user combo but not yet submitted and does not list Grant or NIHMS Repo.
     * Make sure NIHMS Repo and Grant added.
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testAddingToExistingUnsubmittedSubmission() throws Exception {
        PassClientSelector<Publication> pubSelector = new PassClientSelector<>(Publication.class);
        PassClientSelector<Submission> subSelector = new PassClientSelector<>(Submission.class);
        PassClientSelector<RepositoryCopy> repoCopySelector = new PassClientSelector<>(RepositoryCopy.class);
        User user1 = new User();
        passClient.createObject(user1);
        User user2 = new User();
        passClient.createObject(user2);

        String grantId1 = createGrant(grant1, user1);
        String grant2 = "R02 CD123456";
        String grantId2 = createGrant(grant2, user2);

        //we should start with no publication for this pmid
        pubSelector.setFilter(RSQL.equals("pmid", pmid1));
        Optional<Publication> testPub = passClient.streamObjects(pubSelector).findAny();
        assertFalse(testPub.isPresent());

        //create existing publication
        Publication publication = newPublication();
        passClient.createObject(publication);

        //a submission existed for the user/pub combo and is unsubmitted, but has a different grant/repo
        Submission preexistingSub = newSubmission1(grantId1, publication.getId(), user1, false,
                SubmissionStatus.MANUSCRIPT_REQUIRED);
        preexistingSub.setSubmitted(false);
        preexistingSub.setSource(Source.PASS);
        List<Grant> grants = new ArrayList<>();
        PassClientSelector<Grant> addGrantSel = new PassClientSelector<>(Grant.class);
        addGrantSel.setFilter(RSQL.equals("id", grantId2));
        grants.add(passClient.streamObjects(addGrantSel).findFirst().orElseThrow());
        preexistingSub.setGrants(grants);
        List<Repository> repos = new ArrayList<>();
        Repository testRepo = new Repository();
        passClient.createObject(testRepo);
        repos.add(testRepo);
        preexistingSub.setRepositories(repos);
        passClient.createObject(preexistingSub);

        //now make sure we wait for submission, should only be one from the test
        subSelector.setFilter(RSQL.equals("publication.id", publication.getId()));
        List<Submission> testSub = passClient.streamObjects(subSelector).toList();
        assertEquals(1, testSub.size());

        //now we have an existing publication, submission for same user/publication...
        //do transform/load to make sure we get an updated submission that includes grant/repo
        NihmsPublication pub = newNonCompliantNihmsPub();
        NihmsTransformLoadService transformLoadService = new NihmsTransformLoadService(nihmsPassClientService,
                                                                                       mockPmidLookup, statusService);
        transformLoadService.transformAndLoadNihmsPub(pub);

        Submission reloadedPreexistingSub = nihmsPassClientService.readSubmission(preexistingSub.getId());
        assertFalse(reloadedPreexistingSub.getSubmitted());
        assertTrue(reloadedPreexistingSub.getRepositories()
                .contains(nihmsPassClientService.readRepository(ConfigUtil.getNihmsRepositoryId())));
        assertTrue(reloadedPreexistingSub.getRepositories().contains(testRepo));
        assertEquals(2, reloadedPreexistingSub.getRepositories().size());
        assertTrue(reloadedPreexistingSub.getGrants().stream().map(Grant::getId).toList()
                .contains(grantId1));
        assertTrue(reloadedPreexistingSub.getGrants().stream().map(Grant::getId).toList()
                .contains(grantId2));
        assertEquals(2, reloadedPreexistingSub.getGrants().size());
        assertEquals(SubmissionStatus.MANUSCRIPT_REQUIRED, reloadedPreexistingSub.getSubmissionStatus());

        //we should have ONLY ONE submission for this pmid
        PassClientSelector<Submission> subSelOne = new PassClientSelector<>(Submission.class);
        subSelOne.setFilter(RSQL.equals("publication.id", publication.getId()));
        assertEquals(1, passClient.selectObjects(subSelOne).getObjects().size());

        //we should have ONLY ONE publication for this pmid
        PassClientSelector<Publication> pubSelOne = new PassClientSelector<>(Publication.class);
        pubSelOne.setFilter(RSQL.equals("pmid", pmid1));
        assertEquals(1, passClient.selectObjects(pubSelOne).getObjects().size());

        //we should have ZERO Repository Copies for this publication
        repoCopySelector.setFilter(RSQL.equals("publication.id", publication.getId()));
        assertEquals(0, passClient.selectObjects(repoCopySelector).getObjects().size());
    }

    private NihmsPublication newNonCompliantNihmsPub() {
        return new NihmsPublication(NihmsStatus.NON_COMPLIANT, pmid1, grant1, null, null, null, null, null, null,
                                    title);
    }

    private NihmsPublication newNonCompliantStalledNihmsPub() {
        String dateval = "12/12/2017";
        return new NihmsPublication(NihmsStatus.NON_COMPLIANT, pmid1, grant1, nihmsId1, null, dateval, dateval, null,
                                    null, title);
    }

    private Publication newPublication() throws Exception {
        Publication publication = new Publication();
        publication.setDoi(doi);
        publication.setPmid(pmid1);
        publication.setIssue(issue);
        publication.setTitle(title);
        return publication;
    }

    private Submission newSubmission1(String grantId, String pubId, User user, boolean submitted,
                                      SubmissionStatus status) throws Exception {
        Submission submission1 = new Submission();
        List<Grant> grants = new ArrayList<>();
        PassClientSelector<Publication> pubSelect = new PassClientSelector<>(Publication.class);
        pubSelect.setFilter(RSQL.equals("id", pubId));
        Publication pub = passClient.streamObjects(pubSelect).findFirst().orElseThrow();
        PassClientSelector<Grant> grantSelect = new PassClientSelector<>(Grant.class);
        grantSelect.setFilter(RSQL.equals("id", grantId));
        Grant grant = passClient.streamObjects(grantSelect).findFirst().orElseThrow();
        grants.add(grant);
        submission1.setGrants(grants);
        submission1.setPublication(pub);
        submission1.setSubmitter(user);
        submission1.setSource(Source.OTHER);
        submission1.setSubmitted(submitted);
        submission1.setSubmissionStatus(status);
        List<Repository> repos = new ArrayList<>();
        repos.add(new Repository(ConfigUtil.getNihmsRepositoryId()));
        submission1.setRepositories(repos);
        return submission1;
    }

    //this validation does not check repo copy status as it varies for non-compliant
    private void validateRepositoryCopy(RepositoryCopy repoCopy) {
        //check fields in repoCopy
        assertNotNull(repoCopy);
        assertEquals(1, repoCopy.getExternalIds().size());
        assertEquals(nihmsId1, repoCopy.getExternalIds().get(0));
        assertEquals(ConfigUtil.getNihmsRepositoryId(), repoCopy.getRepository().getId());
        assertNull(repoCopy.getAccessUrl());
    }

}
