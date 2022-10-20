# Introduction

This module is responsible for PASS backend services.

# Building

Java 11 and Maven 3.8 required.

```
mvn clean package
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

By default, pass-core-main, will run with an in memory database. In order to use Postgres, switch to the production profile and set environment variables as below.
Note that the system property javax.persistence.schema-generation.database.action can be used to automatically create database tables.

Environment variables:
* spring_profiles_active=production
* PASS_CORE_DATABASE_URL=jdbc:postgresql://postgres:5432/pass
* PASS_CORE_DATABASE_USERNAME=pass
* PASS_CORE_DATABASE_PASSWORD=moo
* PASS_CORE_PORT=8080
* PASS_CORE_JAVA_OPTS="-Djavax.persistence.schema-generation.database.action=create"
* PASS_CORE_BASE_URL
  * Used when building relationship links. This property does not have a default value and must be defined in your environment. The `pass-core-main/.env` is intended to be used for local testing of pass-core in isolation. If we want to use this in the local PASS demo environment, for example, we would specify `PASS_CORE_BASE_URL=https://pass.local`


# Using JSON API

JSON API is deployed at `/data`. All of our data model is available, just divided into attributes and relationshiops. Note that identifiers are now integers, not URIs.

## Creating a RepositoryCopy

```
curl -v -X POST "http://localhost:8080/data/repositoryCopy" -H "accept: application/vnd.api+json" -H "Content-Type: application/vnd.api+json" -d @rc1.json
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
curl -X PATCH "http://localhost:8080/data/journal/1" -H "accept: application/vnd.api+json" -H "Content-Type: application/vnd.api+json" -d @patch.json
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
