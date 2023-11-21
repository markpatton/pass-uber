--
-- TOC entry 209 (class 1259 OID 24873)
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- TOC entry 211 (class 1259 OID 24877)
-- Name: pass_contributor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_contributor (
     id bigint NOT NULL,
     affiliation character varying(255),
     displayname character varying(255),
     email character varying(255),
     firstname character varying(255),
     lastname character varying(255),
     middlename character varying(255),
     orcidid character varying(255),
     roles character varying(255),
     publication_id bigint,
     user_id bigint
);


--
-- TOC entry 212 (class 1259 OID 24884)
-- Name: pass_deposit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_deposit (
     id bigint NOT NULL,
     depositstatus character varying(255),
     depositstatusref character varying(255),
     repository_id bigint,
     repositorycopy_id bigint,
     submission_id bigint
);


--
-- TOC entry 213 (class 1259 OID 24891)
-- Name: pass_file; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_file (
      id bigint NOT NULL,
      description character varying(255),
      filerole character varying(255),
      mimetype character varying(255),
      name character varying(255),
      uri bytea,
      submission_id bigint
);


--
-- TOC entry 214 (class 1259 OID 24898)
-- Name: pass_funder; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_funder (
        id bigint NOT NULL,
        localkey character varying(255),
        name character varying(255),
        url bytea,
        policy_id bigint
);


--
-- TOC entry 215 (class 1259 OID 24905)
-- Name: pass_grant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_grant (
       id bigint NOT NULL,
       awarddate timestamp without time zone,
       awardnumber character varying(255),
       awardstatus character varying(255),
       enddate timestamp without time zone,
       localkey character varying(255),
       projectname character varying(255),
       startdate timestamp without time zone,
       directfunder_id bigint,
       pi_id bigint,
       primaryfunder_id bigint
);


--
-- TOC entry 216 (class 1259 OID 24912)
-- Name: pass_grant_copis; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_grant_copis (
         grant_id bigint NOT NULL,
         copis_id bigint NOT NULL
);


--
-- TOC entry 217 (class 1259 OID 24915)
-- Name: pass_journal; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_journal (
         id bigint NOT NULL,
         journalname character varying(255),
         nlmta character varying(255),
         pmcparticipation character varying(255),
         publisher_id bigint
);


--
-- TOC entry 210 (class 1259 OID 24874)
-- Name: pass_journal_issns; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_journal_issns (
       journal_id bigint NOT NULL,
       issns character varying(255)
);


--
-- TOC entry 218 (class 1259 OID 24922)
-- Name: pass_policy; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_policy (
        id bigint NOT NULL,
        description text,
        institution bytea,
        policyurl bytea,
        title character varying(255)
);


--
-- TOC entry 219 (class 1259 OID 24929)
-- Name: pass_policy_repositories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_policy_repositories (
         policy_id bigint NOT NULL,
         repositories_id bigint NOT NULL
);


--
-- TOC entry 220 (class 1259 OID 24932)
-- Name: pass_publication; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_publication (
         id bigint NOT NULL,
         doi character varying(255),
         issue character varying(255),
         pmid character varying(255),
         publicationabstract text,
         title character varying(255),
         volume character varying(255),
         journal_id bigint
);


--
-- TOC entry 221 (class 1259 OID 24939)
-- Name: pass_publisher; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_publisher (
       id bigint NOT NULL,
       name character varying(255),
       pmcparticipation character varying(255)
);


--
-- TOC entry 222 (class 1259 OID 24946)
-- Name: pass_repository; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_repository (
        id bigint NOT NULL,
        agreementtext text,
        description character varying(255),
        formschema text,
        integrationtype character varying(255),
        name character varying(255),
        repositorykey character varying(255),
        schemas character varying(255),
        url bytea
);


--
-- TOC entry 223 (class 1259 OID 24953)
-- Name: pass_repository_copy; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_repository_copy (
         id bigint NOT NULL,
         accessurl bytea,
         copystatus character varying(255),
         externalids character varying(255),
         publication_id bigint,
         repository_id bigint
);


--
-- TOC entry 224 (class 1259 OID 24960)
-- Name: pass_submission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_submission (
        id bigint NOT NULL,
        aggregateddepositstatus character varying(255),
        metadata text,
        source character varying(255),
        submissionstatus character varying(255),
        submitted boolean,
        submitteddate timestamp without time zone,
        submitteremail bytea,
        submittername character varying(255),
        publication_id bigint,
        submitter_id bigint
);


--
-- TOC entry 225 (class 1259 OID 24967)
-- Name: pass_submission_effectivepolicies; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_submission_effectivepolicies (
      submission_id bigint NOT NULL,
      effectivepolicies_id bigint NOT NULL
);


--
-- TOC entry 226 (class 1259 OID 24970)
-- Name: pass_submission_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_submission_event (
      id bigint NOT NULL,
      comment character varying(255),
      eventtype character varying(255),
      link bytea,
      performeddate timestamp without time zone,
      performerrole character varying(255),
      performedby_id bigint,
      submission_id bigint
);


--
-- TOC entry 227 (class 1259 OID 24977)
-- Name: pass_submission_grants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_submission_grants (
       submission_id bigint NOT NULL,
       grants_id bigint NOT NULL
);


--
-- TOC entry 228 (class 1259 OID 24980)
-- Name: pass_submission_preparers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_submission_preparers (
      submission_id bigint NOT NULL,
      preparers_id bigint NOT NULL
);


--
-- TOC entry 229 (class 1259 OID 24983)
-- Name: pass_submission_repositories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_submission_repositories (
         submission_id bigint NOT NULL,
         repositories_id bigint NOT NULL
);


--
-- TOC entry 230 (class 1259 OID 24986)
-- Name: pass_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_user (
      id bigint NOT NULL,
      affiliation character varying(255),
      displayname character varying(255),
      email character varying(255),
      firstname character varying(255),
      lastname character varying(255),
      middlename character varying(255),
      orcidid character varying(255),
      roles character varying(255),
      username character varying(255)
);


--
-- TOC entry 231 (class 1259 OID 24993)
-- Name: pass_user_locators; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pass_user_locators (
       user_id bigint NOT NULL,
       locatorids character varying(255)
);


--
-- TOC entry 4214 (class 2606 OID 24883)
-- Name: pass_contributor pass_contributor_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_contributor ADD CONSTRAINT pass_contributor_pkey PRIMARY KEY (id);


--
-- TOC entry 4216 (class 2606 OID 24890)
-- Name: pass_deposit pass_deposit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_deposit
    ADD CONSTRAINT pass_deposit_pkey PRIMARY KEY (id);


--
-- TOC entry 4218 (class 2606 OID 24897)
-- Name: pass_file pass_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_file
    ADD CONSTRAINT pass_file_pkey PRIMARY KEY (id);


--
-- TOC entry 4220 (class 2606 OID 24904)
-- Name: pass_funder pass_funder_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_funder
    ADD CONSTRAINT pass_funder_pkey PRIMARY KEY (id);


--
-- TOC entry 4222 (class 2606 OID 24911)
-- Name: pass_grant pass_grant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_grant
    ADD CONSTRAINT pass_grant_pkey PRIMARY KEY (id);


--
-- TOC entry 4224 (class 2606 OID 24921)
-- Name: pass_journal pass_journal_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_journal
    ADD CONSTRAINT pass_journal_pkey PRIMARY KEY (id);


--
-- TOC entry 4226 (class 2606 OID 24928)
-- Name: pass_policy pass_policy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_policy
    ADD CONSTRAINT pass_policy_pkey PRIMARY KEY (id);


--
-- TOC entry 4228 (class 2606 OID 24938)
-- Name: pass_publication pass_publication_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_publication
    ADD CONSTRAINT pass_publication_pkey PRIMARY KEY (id);


--
-- TOC entry 4230 (class 2606 OID 24945)
-- Name: pass_publisher pass_publisher_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_publisher
    ADD CONSTRAINT pass_publisher_pkey PRIMARY KEY (id);


--
-- TOC entry 4234 (class 2606 OID 24959)
-- Name: pass_repository_copy pass_repository_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_repository_copy
    ADD CONSTRAINT pass_repository_copy_pkey PRIMARY KEY (id);


--
-- TOC entry 4232 (class 2606 OID 24952)
-- Name: pass_repository pass_repository_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_repository
    ADD CONSTRAINT pass_repository_pkey PRIMARY KEY (id);


--
-- TOC entry 4238 (class 2606 OID 24976)
-- Name: pass_submission_event pass_submission_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_event
    ADD CONSTRAINT pass_submission_event_pkey PRIMARY KEY (id);


--
-- TOC entry 4236 (class 2606 OID 24966)
-- Name: pass_submission pass_submission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission
    ADD CONSTRAINT pass_submission_pkey PRIMARY KEY (id);


--
-- TOC entry 4240 (class 2606 OID 24992)
-- Name: pass_user pass_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_user
    ADD CONSTRAINT pass_user_pkey PRIMARY KEY (id);


--
-- TOC entry 4211 (class 1259 OID 25259)
-- Name: pass_journal_issns_id_ix; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pass_journal_issns_id_ix ON public.pass_journal_issns USING btree (journal_id);


--
-- TOC entry 4212 (class 1259 OID 25260)
-- Name: pass_journal_issns_issn_ix; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pass_journal_issns_issn_ix ON public.pass_journal_issns USING btree (issns);


--
-- TOC entry 4241 (class 1259 OID 25261)
-- Name: pass_user_locatorids_id_ix; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pass_user_locatorids_id_ix ON public.pass_user_locators USING btree (user_id);


--
-- TOC entry 4242 (class 1259 OID 25262)
-- Name: pass_user_locatorids_locator_ix; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pass_user_locatorids_locator_ix ON public.pass_user_locators USING btree (locatorids);


--
-- TOC entry 4260 (class 2606 OID 25086)
-- Name: pass_repository_copy fk1bbvniv22hciuutpy2j6wcacs; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_repository_copy
    ADD CONSTRAINT fk1bbvniv22hciuutpy2j6wcacs FOREIGN KEY (repository_id) REFERENCES public.pass_repository(id);


--
-- TOC entry 4254 (class 2606 OID 25051)
-- Name: pass_grant_copis fk26789bctqgem7ldbmfd4u601j; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_grant_copis
    ADD CONSTRAINT fk26789bctqgem7ldbmfd4u601j FOREIGN KEY (copis_id) REFERENCES public.pass_user(id);


--
-- TOC entry 4270 (class 2606 OID 25136)
-- Name: pass_submission_preparers fk3jqhlq8ftydmmt37dudfao73d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_preparers
    ADD CONSTRAINT fk3jqhlq8ftydmmt37dudfao73d FOREIGN KEY (submission_id) REFERENCES public.pass_submission(id);


--
-- TOC entry 4257 (class 2606 OID 25066)
-- Name: pass_policy_repositories fk4rxlah1pnav6d9jsek912hgep; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_policy_repositories
    ADD CONSTRAINT fk4rxlah1pnav6d9jsek912hgep FOREIGN KEY (repositories_id) REFERENCES public.pass_repository(id);


--
-- TOC entry 4264 (class 2606 OID 25106)
-- Name: pass_submission_effectivepolicies fk5h8ok2osw799h46vjah39588d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_effectivepolicies
    ADD CONSTRAINT fk5h8ok2osw799h46vjah39588d FOREIGN KEY (submission_id) REFERENCES public.pass_submission(id);


--
-- TOC entry 4250 (class 2606 OID 25031)
-- Name: pass_funder fk6sgcgecdp5qvpwg1orm7vuho8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_funder
    ADD CONSTRAINT fk6sgcgecdp5qvpwg1orm7vuho8 FOREIGN KEY (policy_id) REFERENCES public.pass_policy(id);


--
-- TOC entry 4262 (class 2606 OID 25096)
-- Name: pass_submission fk7lh2cfqoc35witk0wq3htumxd; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission
    ADD CONSTRAINT fk7lh2cfqoc35witk0wq3htumxd FOREIGN KEY (submitter_id) REFERENCES public.pass_user(id);


--
-- TOC entry 4265 (class 2606 OID 25101)
-- Name: pass_submission_effectivepolicies fk81ulhu9fwa8hm025ycc0an7kg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_effectivepolicies
    ADD CONSTRAINT fk81ulhu9fwa8hm025ycc0an7kg FOREIGN KEY (effectivepolicies_id) REFERENCES public.pass_policy(id);


--
-- TOC entry 4256 (class 2606 OID 25061)
-- Name: pass_journal fk838v1tdbhfqafse9c8jbughs9; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_journal
    ADD CONSTRAINT fk838v1tdbhfqafse9c8jbughs9 FOREIGN KEY (publisher_id) REFERENCES public.pass_publisher(id);


--
-- TOC entry 4243 (class 2606 OID 24996)
-- Name: pass_journal_issns fka3vihisdca2tnceedqnr07tfb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_journal_issns
    ADD CONSTRAINT fka3vihisdca2tnceedqnr07tfb FOREIGN KEY (journal_id) REFERENCES public.pass_journal(id);


--
-- TOC entry 4246 (class 2606 OID 25011)
-- Name: pass_deposit fkdw6tvpeeesknmdw3ll00l6ho7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_deposit
    ADD CONSTRAINT fkdw6tvpeeesknmdw3ll00l6ho7 FOREIGN KEY (repository_id) REFERENCES public.pass_repository(id);


--
-- TOC entry 4266 (class 2606 OID 25116)
-- Name: pass_submission_event fke7armxpqxwtw1ohy4kd6tkbd7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_event
    ADD CONSTRAINT fke7armxpqxwtw1ohy4kd6tkbd7 FOREIGN KEY (submission_id) REFERENCES public.pass_submission(id);


--
-- TOC entry 4268 (class 2606 OID 25121)
-- Name: pass_submission_grants fkeqpd9l7agd9smw6v85m9r3n1r; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_grants
    ADD CONSTRAINT fkeqpd9l7agd9smw6v85m9r3n1r FOREIGN KEY (grants_id) REFERENCES public.pass_grant(id);


--
-- TOC entry 4267 (class 2606 OID 25111)
-- Name: pass_submission_event fkfr8d2mo9ykkuv8i55yo7bo1by; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_event
    ADD CONSTRAINT fkfr8d2mo9ykkuv8i55yo7bo1by FOREIGN KEY (performedby_id) REFERENCES public.pass_user(id);


--
-- TOC entry 4272 (class 2606 OID 25146)
-- Name: pass_submission_repositories fkfyeihvs9di8805lspytuv1non; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_repositories
    ADD CONSTRAINT fkfyeihvs9di8805lspytuv1non FOREIGN KEY (submission_id) REFERENCES public.pass_submission(id);


--
-- TOC entry 4263 (class 2606 OID 25091)
-- Name: pass_submission fkgc6wgnkfjix9p3a748gi04mvq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission
    ADD CONSTRAINT fkgc6wgnkfjix9p3a748gi04mvq FOREIGN KEY (publication_id) REFERENCES public.pass_publication(id);


--
-- TOC entry 4274 (class 2606 OID 25151)
-- Name: pass_user_locators fkgnwffd7s0sq183fr00etco0gt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_user_locators
    ADD CONSTRAINT fkgnwffd7s0sq183fr00etco0gt FOREIGN KEY (user_id) REFERENCES public.pass_user(id);


--
-- TOC entry 4247 (class 2606 OID 25021)
-- Name: pass_deposit fki1kwgdhxxp27srh94twrg20xv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_deposit
    ADD CONSTRAINT fki1kwgdhxxp27srh94twrg20xv FOREIGN KEY (submission_id) REFERENCES public.pass_submission(id);


--
-- TOC entry 4255 (class 2606 OID 25056)
-- Name: pass_grant_copis fkiiic6w2fu4ye3g96hgl7p9lx2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_grant_copis
    ADD CONSTRAINT fkiiic6w2fu4ye3g96hgl7p9lx2 FOREIGN KEY (grant_id) REFERENCES public.pass_grant(id);


--
-- TOC entry 4244 (class 2606 OID 25006)
-- Name: pass_contributor fkiixn812ow04w4ba40ryk441b6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_contributor
    ADD CONSTRAINT fkiixn812ow04w4ba40ryk441b6 FOREIGN KEY (user_id) REFERENCES public.pass_user(id);


--
-- TOC entry 4248 (class 2606 OID 25016)
-- Name: pass_deposit fkm60rx7vwtbsboarrpve90j9u7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_deposit
    ADD CONSTRAINT fkm60rx7vwtbsboarrpve90j9u7 FOREIGN KEY (repositorycopy_id) REFERENCES public.pass_repository_copy(id);


--
-- TOC entry 4251 (class 2606 OID 25041)
-- Name: pass_grant fkmbari9ib5tv98j7qtauud4jba; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_grant
    ADD CONSTRAINT fkmbari9ib5tv98j7qtauud4jba FOREIGN KEY (pi_id) REFERENCES public.pass_user(id);


--
-- TOC entry 4261 (class 2606 OID 25081)
-- Name: pass_repository_copy fkn1r1sfjy9y62yaw2ug688tlo0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_repository_copy
    ADD CONSTRAINT fkn1r1sfjy9y62yaw2ug688tlo0 FOREIGN KEY (publication_id) REFERENCES public.pass_publication(id);


--
-- TOC entry 4245 (class 2606 OID 25001)
-- Name: pass_contributor fkn3uscetscw2e4lki997jcnopq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_contributor
    ADD CONSTRAINT fkn3uscetscw2e4lki997jcnopq FOREIGN KEY (publication_id) REFERENCES public.pass_publication(id);


--
-- TOC entry 4269 (class 2606 OID 25126)
-- Name: pass_submission_grants fkn8ql67dc8o4468q400m7r84ha; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_grants
    ADD CONSTRAINT fkn8ql67dc8o4468q400m7r84ha FOREIGN KEY (submission_id) REFERENCES public.pass_submission(id);


--
-- TOC entry 4271 (class 2606 OID 25131)
-- Name: pass_submission_preparers fkncqfwdnh29u0x6mhtpq0t61xe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_preparers
    ADD CONSTRAINT fkncqfwdnh29u0x6mhtpq0t61xe FOREIGN KEY (preparers_id) REFERENCES public.pass_user(id);


--
-- TOC entry 4252 (class 2606 OID 25046)
-- Name: pass_grant fknuiqdqiwg0vk42x7kdgnksi22; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_grant
    ADD CONSTRAINT fknuiqdqiwg0vk42x7kdgnksi22 FOREIGN KEY (primaryfunder_id) REFERENCES public.pass_funder(id);


--
-- TOC entry 4259 (class 2606 OID 25076)
-- Name: pass_publication fknvi7ib065xdgs5qnia2hdq0ag; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_publication
    ADD CONSTRAINT fknvi7ib065xdgs5qnia2hdq0ag FOREIGN KEY (journal_id) REFERENCES public.pass_journal(id);


--
-- TOC entry 4258 (class 2606 OID 25071)
-- Name: pass_policy_repositories fkox0wj7wioupm849dbx07vvrfp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_policy_repositories
    ADD CONSTRAINT fkox0wj7wioupm849dbx07vvrfp FOREIGN KEY (policy_id) REFERENCES public.pass_policy(id);


--
-- TOC entry 4273 (class 2606 OID 25141)
-- Name: pass_submission_repositories fkre4lr2vhbmo6gxcqwf846gew2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_submission_repositories
    ADD CONSTRAINT fkre4lr2vhbmo6gxcqwf846gew2 FOREIGN KEY (repositories_id) REFERENCES public.pass_repository(id);


--
-- TOC entry 4249 (class 2606 OID 25026)
-- Name: pass_file fksxfobffga3ejir7099hyaqf71; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_file
    ADD CONSTRAINT fksxfobffga3ejir7099hyaqf71 FOREIGN KEY (submission_id) REFERENCES public.pass_submission(id);


--
-- TOC entry 4253 (class 2606 OID 25036)
-- Name: pass_grant fktifdj59wn3vwf6xlkjypf5xil; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE public.pass_grant
    ADD CONSTRAINT fktifdj59wn3vwf6xlkjypf5xil FOREIGN KEY (directfunder_id) REFERENCES public.pass_funder(id);

