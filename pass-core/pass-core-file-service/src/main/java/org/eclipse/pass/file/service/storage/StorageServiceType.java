package org.eclipse.pass.file.service.storage;

/**
 * The StorageServiceType enum defines the type of storage supported by the File Service. The two types of
 * persistence are supported: File Systems and S3 buckets. These values are to be used in the environment var
 * configuration. If a new type of persistence is to be added, it must be added to this enum.
 */
public enum StorageServiceType {
    /**
     * 'FILE_SYSTEM' is the type of storage that uses a local file system to store the files.
     */
    FILE_SYSTEM("FILE_SYSTEM"),
    /**
     * 'S3' is the type of storage that uses an S3 bucket to store the files.
     */
    S3("S3");

    /**
     * Get the string value of the StorageServiceType. Can be either an 'FILE_SYSTEM' or 'S3'
     */
    public final String label;
    StorageServiceType(String label) {
        this.label = label;
    }
}
