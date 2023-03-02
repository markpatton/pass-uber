package org.eclipse.pass.object.security;

import java.util.Optional;

import com.yahoo.elide.annotation.SecurityCheck;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import com.yahoo.elide.core.security.checks.OperationCheck;
import org.eclipse.pass.object.model.File;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.SubmissionEvent;
import org.eclipse.pass.object.model.User;

/**
 * Check that a user is the submitter or preparer of a submission associated
 * with an object.
 */
@SecurityCheck(PartOfSubmissionCheck.OBJECT_PART_OF_USER_SUBMISSION)
public class PartOfSubmissionCheck<T> extends OperationCheck<T> {
    public static final String OBJECT_PART_OF_USER_SUBMISSION = "Object part of User Submission";

    @Override
    public boolean ok(T obj, RequestScope requestScope, Optional<ChangeSpec> changeSpec) {
        String user_name = requestScope.getUser().getName();

        if (obj instanceof File) {
            File file = File.class.cast(obj);

            return part_of(user_name, file.getSubmission());
        } else if (obj instanceof Submission) {
            Submission sub = Submission.class.cast(obj);

            return part_of(user_name, sub);
        } else if (obj instanceof SubmissionEvent) {
            SubmissionEvent ev = SubmissionEvent.class.cast(obj);

            return part_of(user_name, ev.getSubmission());
        } else {
            return false;
        }
    }

    private boolean part_of(String user_name, Submission sub) {
        if (sub == null) {
            return false;
        }

        if (sub.getSubmitter() != null && user_name.equals(sub.getSubmitter().getUsername())) {
            return true;
        }

        for (User p : sub.getPreparers()) {
            if (p.getUsername() != null && user_name.equals(p.getUsername())) {
                return true;
            }
        }

        return false;
    }
}