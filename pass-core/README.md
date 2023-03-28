# Introduction

This module is a Spring Boot application which provides the PASS REST API.

# Building

Java 11 and Maven 3.8 required.

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

Environment variables:
* spring_profiles_active=production
* PASS_CORE_DATABASE_URL=jdbc:postgresql://postgres:5432/pass
* PASS_CORE_DATABASE_USERNAME=pass
* PASS_CORE_DATABASE_PASSWORD=moo
* PASS_CORE_PORT=8080
* PASS_CORE_LOG_DIR=${java.io.tmpdir}/pass-core
* PASS_CORE_BACKEND_USER=backend
* PASS_CORE_BACKEND_PASSWORD=moo
* PASS_CORE_JAVA_OPTS="-Djavax.persistence.schema-generation.database.action=create"
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

# JSON API

JSON API is deployed at `/data`. All of our data model is available, just divided into attributes and relationshiops. Note that identifiers are now integers, not URIs.
See https://elide.io/pages/guide/v6/10-jsonapi.html for information on how Elide provides support for filtering and and sorting.

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
      "copyStatus": "accepted"
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

# Debugging problems

To get more information, try changing the logging levels set pass-core-main/src/main/resources/logback-spring.xml.
See https://elide.io/pages/guide/v6/12-audit.html for more information.