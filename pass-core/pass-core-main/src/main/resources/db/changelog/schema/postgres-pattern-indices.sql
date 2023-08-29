-- Make LIKE operations faster for Journal names and Grant award numbers
-- For journal names RSQL ini is used which results in lower() in the SQL which the index must match

-- One approach is to use varchar_pattern_ops
-- CREATE INDEX pass_grant_awardnumber_ix ON public.pass_grant (awardnumber varchar_pattern_ops);
-- CREATE INDEX pass_journal_name_ix ON public.pass_journal (lower(journalname) varchar_pattern_ops);

-- Trigrams seem faster
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX pass_journal_name_ix ON public.pass_journal USING GIN(lower(journalname) gin_trgm_ops);
CREATE INDEX pass_grant_awardnumber_ix ON public.pass_grant USING GIN(awardnumber gin_trgm_ops);
