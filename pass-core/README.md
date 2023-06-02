# Introduction

This module is a Spring Boot application which provides the PASS REST API.

# Building

Java 17 and Maven 3.8 required.

```
mvn clean install
```

This will produce an executabler jar `pass-core-main/target/pass-core-main.jar` and a docker image `ghcr.io/eclipse-pass/pass-core-main`.

# Running local build

```
java -jar pass-core-main.jar
```

By default an in memory database is used.

Look at http://localhost:8080/ to see the auto-created documentation and a UI for testing out the api.

You can directly make request with the UI and see what happens. Note when doing a POST to create an object, be sure to edit the type field to have the correct object type and delete the id field to have the id auto-generated.

## Running with Docker

This uses Postgres.

In pass-core-main run:
```
docker-compose up -d
```

# Configuration

The application is configured by its application.yaml which in turn references a number of environment variables.

By default, pass-core-main, will run with an in memory database. In order to use Postgres, switch to the production profile and set the database environment variables as below.
Note that the system property `javax.persistence.schema-generation.database.action` can be used to automatically create database tables.

If `PASS_CORE_USE_SQS` is `true`, then pass-core will attempt to connect to Amazon SQS. The connection must be configured with `AWS_REGION`, `AWS_ACCESS_KEY_ID`, and `AWS_SECRET_ACCESS_KEY`.
The AWS credentials are also needed if the file service S3 backend is used.

Otherwise a connection to an ActiveMQ broker can be configured by setting `SPRING_ACTIVEMQ_BROKER_URL`. If 'PASS_CORE_EMBED_JMS_BROKER` is true, then an embedded ActiveMQ broker will be started
using that url. This can be useful to set tcp transport for connecting containers in a docker environment. The default is an embedded broker using vm transport.

Environment variables:
* spring_profiles_active=production
* AWS_REGION=us-east-1
* AWS_ACCESS_KEY_ID=xxx
* AWS_SECRET_ACCESS_KEY=xxx
* PASS_CORE_DATABASE_URL=jdbc:postgresql://postgres:5432/pass
* PASS_CORE_DATABASE_USERNAME=pass
* PASS_CORE_DATABASE_PASSWORD=moo
* PASS_CORE_PORT=8080
* PASS_CORE_LOG_DIR=${java.io.tmpdir}/pass-core
* PASS_CORE_BACKEND_USER=backend
* PASS_CORE_BACKEND_PASSWORD=moo
* PASS_CORE_USE_SQS=false
* PASS_CORE_EMBED_JMS_BROKER=true
* PASS_CORE_SUBMISSION_QUEUE=pass-submission
* PASS_CORE_DEPOSIT_QUEUE=pass-deposit
* PASS_CORE_SUBMISSION_EVENT_QUEUE=pass-submission-event
* PASS_CORE_USERTOKEN_KEY=xxx
  * If not present, one is generated. See the [user service](pass-core-user-service/README.md) for how to create manually.
* PASS_CORE_JAVA_OPTS="-Djavax.persistence.schema-generation.database.action=create"
  * Used by the Docker image to pass arguments to Java
* PASS_CORE_BASE_URL=http://localhost:8080
  * Used when services send URLs to the client such as relationship links.

The environment variables in `pass-core-main/.env` are intended to be used for local testing of pass-core in isolation.
For the local PASS demo environment, for example, we would specify `PASS_CORE_BASE_URL=https://pass.local`

# Access control

This application is meant to be deployed behind a proxy which ensures clients are authenticated.
Clients either have a backend or submitter role. The backend can do everything.
The submitter is restricted to creating and modifying certain objects in the data model.
The submitter has full access to all other services.

A request which has gone through the proxy must have headers set which give information about the client.
The client is mapped to a PASS User object. That object is created if the client is formerly unknown. If the
client is already known, the existing client User object updated with any new information. In this case the
client will have a submitter role.

If a request has not gone through the proxy, it must be authenticated with HTTP basic. This is used for requests coming from the backend.
Note the environment variables above which set the backend user credentials.

# User service

The [user service](pass-core-user-service/README.md) provides information about the logged in user.

# DOI service

The [DOI service](pass-core-doi-service/README.md) provides the ability to interact with DOIs.

# File service

The [file service](pass-core-file-service/README.md) provides a mechanism to persist files.

# Metadata Schema service

The [metadata schema service](pass-core-metadataschema-service/README.md) provides JSON schemas intended to describe PASS submission metadata

# Metadata Schema service

The [metadata schema service](pass-core-metadataschema-service/README.md) provides JSON schemas intended to describe PASS submission metadata

# JSON API

JSON API is deployed at `/data`. All of our data model is available, just divided into attributes and relationships. Note that identifiers are now integers, not URIs.
See https://elide.io/pages/guide/v6/10-jsonapi.html for information on how Elide provides support for filtering and sorting.

## Creating a RepositoryCopy

```
curl -v -u backend:moo -X POST "http://localhost:8080/data/repositoryCopy" -H "accept: application/vnd.api+json" -H "Content-Type: application/vnd.api+json" -d @rc1.json
```

*rc1.json:*
```
{
  "data": {
    "type": "repositoryCopy",
    "attributes": {
      "accessUrl": "http://example.com/path",
      "copyStatus": "ACCEPTED"
    }
  }
}
```

## Patch a Journal

Add a publisher object to the publisher relationship in a journal. Note that both the journal and publisher objects must already exist.

```
curl -u backend:moo -X PATCH "http://localhost:8080/data/journal/1" -H "accept: application/vnd.api+json" -H "Content-Type: application/vnd.api+json" -d @patch.json
```

*patch.json:*
 ```
 {
  "data": {
    "type": "journal",
    "id": "1",
    "relationships": {
      "publisher": {
        "data": {
          "id": "2",
          "type": "publisher"
        }
      }
    }
  }
}
```

# Messages

Messages are JSON objects emitted to a JMS broker as text messages. The different types of messages are sent to different queues specified
by the indicatedby the environment variables `PASS_CORE_SUBMISSION_QUEUE`, `PASS_CORE_SUBMISSION_EVENT_QUEUE`, and `PASS_CORE_DEPOSIT_QUEUE`.

When a Submission is created or modified and the submitted field is true, then a SubmissionReady event is emitted.
The id of the Submission will be set in the `submission` field of the message.

When a SubmissionEvent is created, then the a SubmissionEvent message will be sent.
The id of the SubmissionEvent will be set in the `submission-event` field of the message. If the `eventType` field is `APPROVAL_REQUESTED_NEWUSER`,
then an `approval-link` field will be set in the field of the message with a link to be sent to a user.

When a Deposit is created or modified, then a DepositStatus event is emitted.
The id of the Deposit will be set in the `deposit` field of the message.

Example messages:
```
{
    "type": "SubmissionReady",
    "submission": "1"
}

{
    "type": "DepositStatus",
    "deposit": "1"
}

{
    "type": "SubmissionEvent",
    "submission-event": "1",
    "approval-link": "http://example.com/passui?userToken=xxxx"
}
```

# Debugging problems

To get more information, try changing the logging levels set pass-core-main/src/main/resources/logback-spring.xml.
See https://elide.io/pages/guide/v6/12-audit.html for more information.