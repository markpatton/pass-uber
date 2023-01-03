# pass-doi-service

Service for accepting a DOI and returning a Journal ID and Crossref metadata for the DOI via the /doi/journal endpoint, or 
returning information about a manuscript from Unpaywall via the /doi/manuscript endpoint.

## Description for the `/doi/journal` endpoint

This service accepts a journal DOI as a query parameter:

`http://<host>:<port>/doi/journal?doi=<doi>`

DOIs must contain a form like `10.1234/ ...`
If a DOI is of a longer URL form containing the string `doi.org/`, then we truncate the DOI to take everything after
this substring.

The service validates the form of the doi - if it is valid, then we hit the Crossref API to get information about the
corresponding journal. We then check to see if there is a
`Journal` object in PASS for this journal. If not we create one. The service then returns to the caller a JSON object
containing the `journal-id` of the PASS journal, and a `crossref` object representing the data returned to the service
as a result of the Crossref call.

### Configuration

The service will look for an environment variable called PASS_DOI_SERVICE_MAILTO to specify a value on the User-Agent
header on the Crossref request. Default value os `pass@jhu/edu`.

## Description for the `/doi/manuscript` endpoint

This service accepts a manuscript DOI as a query parameter:

`http://<host>:<port>/doi/manuscript?doi=<doi>`

DOIs must contain a form like `10.1234/ ...`
If a DOI is of a longer URL form containing the string `doi.org/`, then we truncate the DOI to take everything after
this substring.

The service validates the form of the doi - if it is valid, then we hit the Unpaywall API to get information about the
corresponding locations on the web for manuscript PDFs related to the article referenced by the DOI.

### Configuration

Just as for the /doi/journal endpoint, this service will look for an environment variable called PASS_DOI_SERVICE_MAILTO 
to specify a value for the `email` query parameter on the Unpaywall request. In addition, we may supply values for XREF_BASEURI
and UNPAYWALL_BASEURI, which default to `https://api.crossref.org/v1/works/` and `https://api.unpaywall.org/v2/` respectively.