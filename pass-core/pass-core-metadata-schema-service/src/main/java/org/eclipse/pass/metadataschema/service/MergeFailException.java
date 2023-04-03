package org.eclipse.pass.metadataschema.service;

public class MergeFailException extends RuntimeException {
    public MergeFailException(String errorMessage) {
        super(errorMessage);
    }

}
