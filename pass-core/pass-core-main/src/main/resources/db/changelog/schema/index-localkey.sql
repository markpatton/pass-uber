-- Index Grant.localKey
CREATE INDEX pass_grant_localkey_ix ON public.pass_grant (localkey);

-- Index Funder.localKey
CREATE INDEX pass_funder_localkey_ix ON public.pass_funder (localkey);
