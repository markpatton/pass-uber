package org.eclipse.pass.file.service.storage;

/**
 * The StorageServiceType enum defines the type of storage supported by the File Service. The two types of
 * persistence are supported: File Systems and S3 buckets. These values are to be used in the environment var
 * configuration. If a new type of persistence is to be added, it must be added to this enum.
 */
public enum StorageServiceType {
    FILE_SYSTEM("FILE_SYSTEM"),
    S3("S3");

    public final String label;
    private StorageServiceType(String label) {
        this.label = label;
    }
}
