-- Add indices for columns that are searched including most foreign keys

CREATE INDEX pass_grant_pi_ix ON public.pass_grant (pi_id);

CREATE INDEX pass_grant_copis_grant_ix ON public.pass_grant_copis (grant_id);
CREATE INDEX pass_grant_copis_copi_ix ON public.pass_grant_copis (copis_id);

CREATE INDEX pass_deposit_submission_ix ON public.pass_deposit (submission_id);
CREATE INDEX pass_deposit_status_ix ON public.pass_deposit (depositstatus);

CREATE INDEX pass_file_submission_ix ON public.pass_file (submission_id);

CREATE INDEX pass_submission_status_ix ON public.pass_submission (submissionstatus);
CREATE INDEX pass_submission_submitter_ix ON public.pass_submission (submitter_id);
CREATE INDEX pass_submission_publication_ix ON public.pass_submission (publication_id);

CREATE INDEX pass_submission_preparers_submission_ix ON public.pass_submission_preparers (submission_id);
CREATE INDEX pass_submission_preparers_preparer_ix ON public.pass_submission_preparers (preparers_id);

CREATE INDEX pass_submission_effectivepolicies_submission_ix ON public.pass_submission_effectivepolicies (submission_id);

CREATE INDEX pass_submission_event_submission_ix ON public.pass_submission_event (submission_id);

CREATE INDEX pass_policy_repositories_policy_ix ON public.pass_policy_repositories (policy_id);

CREATE INDEX pass_repository_copy_publication_ix ON public.pass_repository_copy (publication_id);

CREATE INDEX pass_repository_copy_externalids_rc_ix ON public.pass_repository_copy_external_ids (repositorycopy_id);
