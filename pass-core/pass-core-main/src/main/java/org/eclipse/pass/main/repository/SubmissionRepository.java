package org.eclipse.pass.main.repository;

import org.eclipse.pass.object.model.Submission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface SubmissionRepository extends CrudRepository<Submission, String> {

    @Query("select s.version from Submission s where s.id = ?1")
    Long findSubmissionVersionById(Long submissionId);
}
