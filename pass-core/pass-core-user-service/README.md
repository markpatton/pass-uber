# User service

The user service provides information about the currently authenticated user.

The endpoint is `/user/whoami` which will return a JSON object on a GET request.
The JSON object tells the client which PASS object represents the authenticated user.

Example result:
```
{
  "id": "1234",
  "type": "user",
  "uri": "http://localhost:8080/data/user/1234"
}
```

The parameter `userToken` can be used to provide a user token. The user token must be associated with
a submission and an email address. (A user token is an encrypted tuple consisting of a PASS resource
URI and a reference URI.) A request with a user token will return as normal, but have
the side effect of setting the submitter of the submission to the current user. In order for
that to happen the submitterEmail address field on the submission must match the email address of the token.

Then environment variable PASS_CORE_USERTOKEN_KEY provides the key used to encrypt and decrypt user tokens.
If it is not provided or empty, user token support is disabled. To generate a key run the class,
`org.eclipse.pass.usertoken.KeyGenerator`. One way to do that is from `pass-core-usertoken` to do
`mvn compile exec:java -Dexec.mainClass="org.eclipse.pass.usertoken.KeyGenerator"`.
