update pass_repository_copy
set publication_id = pass_submission.publication_id
from pass_deposit, pass_submission
where pass_deposit.submission_id = pass_submission.id
and pass_repository_copy.id = pass_deposit.repositorycopy_id
and pass_repository_copy.publication_id is null;