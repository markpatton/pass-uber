# pass-core-metadataschema-service

## Description
The pass-core-metadataschema-service is a RESTful service that provides JSON schemas and example data intended to describe PASS submission metadata as per the 
[schemas for forms and validation](https://docs.google.com/document/d/1sLWGZR4kCvQVGv-TA5x8ny-AxL3ChBYNeFYW1eACsDw/edit) design, 
as well as a [schema service](https://docs.google.com/document/d/1Ki6HUYsEkKPduungp5gHmr7T_YrQUiaTipjorcSnf4s/edit) that will retrieve,
dereference, and place in the correct order all schemas relevant to a given set of pass Repositories.

## Schemas

The JSON schemas herein describe the JSON metadata payload of PASS [submission](https://oa-pass.github.io/pass-data-model/documentation/Submission.html) entities.  They serve two purposes
1. Validation of submission metadata
2. Generation of forms in the PASS user interface

These schemas follow a defined structure where properties in `/definitions/form/properties` are intended to be displayed by a UI, e.g.

    {
        "title": "Example schema",
        "description": "NIHMS-specific metadata requirements",
        "$id": "https://github.com/eclipse-pass/metadata-schemas/jhu/example.json",
        "$schema": "http://json-schema.org/draft-07/schema#",
        "type": "object",
        "definitions": {
            "form": {
                "title": "Please provide the following information",
                "type": "object",
                "properties": {
                    "journal": {
                        "$ref": "global.json#/properties/journal"
                    },
                    "ISSN": {
                        "$ref": "global.json#/properties/ISSN"
                    }
                }
            },
        },
        "allOf": [
            {
                "$ref": "global.json#"
            },
            {
                "$ref": "#/definitions/form"
            }
        ]
    }

A pass [repository](https://oa-pass.github.io/pass-data-model/documentation/Repository.html) entity represents a target repository where
submissions may be submitted.  Each repository may link to one or more JSON schemas that define the repository's metadata requirements.
In terms of expressing a desired user interface experience, one may observe a general pattern of pointing to a "common" schema containing ubiquitous fields, 
and additionally pointing to a "repository-specific" schema containing any additional fields that are unique to a given repository.

As a concrete example, the NIHMS repository may point to the [common.json](jhu/common.json) schema, as well as the [nihms.json](jhu/nihms.json)
schema.

## Schema service

The schema service is an http service that accepts a list of PASS [repository](https://oa-pass.github.io/pass-data-model/documentation/Repository.html) 
entity IDs `application/json` or newline delimited `text/plain`, in a POST request.  for example:

    [
        "1",
        "2",
        "3"
    ]

For each repository, the schema service will retrieve the list of schemas relevant to the repository, place that list in the correct order (so
that schemas that provide the most dependencies are displayed first), and resolves all `$ref` references that might appear in the schema.

If a `merge` query parameter is provided (with any value, e.g. `?merge=true`), then all schemas will be merged into a single union schema. 
If the service is unable to merge schemas together, it will respond with `409 Conflict` status. 
In this case, a client can issue a request without the `merge` query parameter to get the un-merged list of schemas.

The result is an `application/json` response that contains a JSON list of schemas.

## HTTP Error Responses
The service will return the following HTTP error responses:
- 400 - Bad Request
  - This is returned when the request body is not valid JSON or when the request body is not a list of repository URIs.
- 409 - Conflict
  - This is returned when a schema is unable to be merged or when a schema fetch failed.
- 500 - Internal Server Error
    - This error is returned when an unexpected error occurs in the service.


## Usage Examples

### Retrieve schemas for a list of repositories

  `curl --location "http://localhost:8080/schemaservice" --header 'Content-Type: application/json' --data "[ "1", "2" ]"`