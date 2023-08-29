-- Needed for ElementCollection on RepositoryCopy externalids
CREATE TABLE public.pass_repository_copy_external_ids (
       repositorycopy_id bigint NOT NULL,
       externalids character varying(255)
);
