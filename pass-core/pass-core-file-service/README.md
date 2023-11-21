# pass-core-file-service

## Description
The pass-core-file-service is a RESTful service that provides the ability to upload, download, and delete files to a
configured persistence store. The service is currently designed to persist to a filesystem or S3 compatible storage.

## Configuration
The service is configured via environment variables. The service by default will use a filesystem based persistence 
store and does not require any additional configuration. If the variable PASS_CORE_FILE_SERVICE_ROOT_DIR does not have 
any value, the File Service will default to the system temp folder and create a temporary root folder of a random value 
in the system temp. The variable PASS_CORE_FILE_SERVICE_ROOT_DIR is used by both the FILE_SYSTEM and S3 service types. 
It is the root directory where temporary files are stored before being persisted to the configured persistence store as 
specified by PASS_CORE_FILE_SERVICE_TYPE. If using FILE_SYSTEM as the persistence store, PASS_CORE_FILE_SERVICE_ROOT_DIR
is also the root directory for file persistence. The value for the PASS_CORE_FILE_SERVICE_ROOT_DIR cannot be a S3 bucket,
and it must be a valid path on the local filesystem.

The following environment variables are available for configuring the service:

- PASS_CORE_FILE_SERVICE_TYPE=`FILE_SYSTEM`
  - Currently supports [`FILE_SYSTEM` | `S3`]
- PASS_CORE_FILE_SERVICE_ROOT_DIR=`/path/to/root/dir`
  - The root directory of the service that is used to support file uploads and downloads and the root directory 
    for file persistence if using FILE_SYSTEM as the persistence store.
  - Default example: system_tmp/17318424270250529523
- PASS_CORE_S3_BUCKET_NAME=`bucket-test-name`
  - The name of the S3 bucket to use for file persistence if using S3 as the persistence store.
- PASS_CORE_S3_REPO_PREFIX=`s3-repo-prefix`
- PASS_CORE_S3_ENDPOINT=`http://localhost:9090`
  - If using a custom endpoint for S3, this value should be set to the endpoint URL.

## HTTP Error Responses
The service will return the following HTTP error responses:
- 400 - Bad Request
  - This error is returned when a file is empty or missing. It will also handle exceptions that are thrown by the OCFL
    library. 
- 404 - Not Found
  - This is returned when performing a GET/DELETE and the fileId is invalid
- 500 - Internal Server Error
  - This error is returned when an unexpected error occurs in the service.

## Usage Examples

### Upload a file

```
curl -X POST "http://localhost:8080/file" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@/path/to/file"
```

### Download a file

```
curl -X GET "http://localhost:8080/file/{uuid}/{origFileName}" -H "accept: application/octet-stream" --output /path/to/file"
```

### Delete a file

```
curl -X DELETE "http://localhost:8080/file/{fileId}/{origFileName}" -H "accept: application/json"
```