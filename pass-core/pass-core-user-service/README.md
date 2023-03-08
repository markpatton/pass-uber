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


