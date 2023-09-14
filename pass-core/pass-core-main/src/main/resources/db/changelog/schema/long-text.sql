
-- Support long for File.descripton
ALTER TABLE public.pass_file ALTER COLUMN description TYPE text;

-- Support long Publication.title
ALTER TABLE public.pass_publication ALTER COLUMN title TYPE text;

-- Support long SubmissionEvent.comment
ALTER TABLE public.pass_submission_event ALTER COLUMN comment TYPE text;


