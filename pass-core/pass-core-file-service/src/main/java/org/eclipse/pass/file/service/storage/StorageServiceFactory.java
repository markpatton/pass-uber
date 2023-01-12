package org.eclipse.pass.file.service.storage;

import org.eclipse.pass.file.service.FileServiceException;
import org.eclipse.pass.file.service.PassFileServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PassFileServiceController.class);

    /**
     * Class constructor
     */
    public StorageServiceFactory() {
    }

    /**
     * Creates the StorageService which is responsible for performing the persistence operations. The type of storage
     * is determined by the StorageProperties which looks at the application.yaml for the PASS_CORE_FILE_SERVICE_TYPE.
     * This environment variable is responsible for determining the type of storage pass-core uses.
     *
     * @return StorageService, the implementation will be determined by the StorageProperties
     * @throws FileServiceException
     */
    public StorageService createStorage(StorageProperties storageProperties) throws FileServiceException {
        LOG.info("createStorage()");
        StorageService storageService = null;
        String storageType = storageProperties.getStorageType();
        LOG.info("After storage type");
        if (storageType != null) {
            switch (storageType.toUpperCase()) {
                case "FILE_SYSTEM":
                    storageService = new FileSystemStorageService(storageProperties);
                    break;
                case "AWS_S3":
                    //TODO will need to implement AWS S3
                    storageService = new AwsStorageService(storageProperties);
                    break;
                default:
                    throw new FileServiceException("File Service: No Storage Type provided. Please check the .env " +
                            "and ensure that a PASS_CORE_FILE_SERVICE_TYPE value is provided");
            }
        }
        storageService.init();
        return storageService;
    }
}
