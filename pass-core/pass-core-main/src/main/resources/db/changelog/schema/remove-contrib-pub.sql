-- Remove Contributor
DROP TABLE public.pass_contributor;

-- Remove Journal.publication
ALTER TABLE public.pass_journal DROP COLUMN publisher_id;

-- Remove Publisher
DROP TABLE public.pass_publisher;