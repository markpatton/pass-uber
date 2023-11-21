INSERT INTO pass_repository_copy_external_ids (repositorycopy_id, externalids)
SELECT id, externalids FROM pass_repository_copy where externalids is not null;