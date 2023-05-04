package org.eclipse.pass.user;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yahoo.elide.RefreshableElide;
import org.eclipse.pass.object.PassClient;
import org.eclipse.pass.object.PassClientResult;
import org.eclipse.pass.object.PassClientSelector;
import org.eclipse.pass.object.RSQL;
import org.eclipse.pass.object.model.Submission;
import org.eclipse.pass.object.model.User;
import org.eclipse.pass.usertoken.BadTokenException;
import org.eclipse.pass.usertoken.Token;
import org.eclipse.pass.usertoken.TokenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The user service reports information about the currently logged in user.
 */
@RestController
public class UserServiceController {
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceController.class);

    private final TokenFactory token_factory;

    @Autowired
    private RefreshableElide refreshableElide;

    /**
     * Construct a UserServiceController.
     *
     * @param usertoken_key or null
     */
    public UserServiceController(@Value("${pass.usertoken.key:#{null}}") String usertoken_key) {
        this.token_factory = usertoken_key == null || usertoken_key.isEmpty() ? null
                : new TokenFactory(usertoken_key);

        if (token_factory == null) {
            LOG.warn("Token support disabled.");
        }
    }

    /**
     * Handles the request for retrieving information about the currently logged in user. The response is a JSON object
     * with the following fields: id, type, and uri.
     *
     * @param request The HTTP request containing the user Principal.
     * @param response The HTTP response containing the JSON object with the following fields: id, type, and uri.
     * @throws IOException if an error occurs while writing the response.
     */
    @GetMapping("/user/whoami")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Principal principal = request.getUserPrincipal();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        if (principal == null || principal.getName() == null) {
            set_error_response(response, "No principal", HttpStatus.UNAUTHORIZED);
            return;
        }

        String user_name = principal.getName();

        try (PassClient client = PassClient.newInstance(refreshableElide)) {
            PassClientSelector<User> selector = new PassClientSelector<>(User.class);
            selector.setFilter(RSQL.equals("username", user_name));

            PassClientResult<User> result = client.selectObjects(selector);

            if (result.getObjects().isEmpty()) {
                set_error_response(response, "No user matching principal: " + user_name,
                        HttpStatus.INTERNAL_SERVER_ERROR);
                return;
            }

            if (result.getObjects().size() > 1) {
                set_error_response(response, "Multiple users matching principal: " + user_name,
                        HttpStatus.INTERNAL_SERVER_ERROR);
                return;
            }

            User user = result.getObjects().get(0);
            Token user_token = get_user_token(request.getQueryString());

            if (user_token != null) {
                enact_user_token(user, user_token, client);
            }

            String url = PassClient.getUrl(refreshableElide, user);
            JsonObject obj = Json.createObjectBuilder().add("id", user.getId().toString()).add("type", "user")
                    .add("uri", url).build();

            set_response(response, obj, HttpStatus.OK);
        } catch (BadTokenException e) {
            set_error_response(response, "Bad user token: " + request.getQueryString(), HttpStatus.BAD_REQUEST);
        }
    }

    private Token get_user_token(String query) throws BadTokenException {
        if (query == null || token_factory == null) {
            return null;
        }

        if (token_factory.hasToken(query)) {
            return token_factory.fromUri(query);
        }

        return null;
    }

    // Make the user the submitter on the submission specified by the token
    public void enact_user_token(User user, Token token, PassClient pass_client) throws IOException, BadTokenException {
        if (!token.getPassResourceType().equals("submission")) {
            throw new BadTokenException(String.format("Expected submission <%s>", token.getPassResource()));
        }

        Submission submission = pass_client.getObject(Submission.class, token.getPassResourceIdentifier());

        if (submission == null) {
            throw new IOException(String.format("Submission <%s> not found", token.getPassResource()));
        }

        if (token.getReference().equals(submission.getSubmitterEmail())) {
            LOG.info("User <{}> will be made a submitter for <{}>, based on matching e-mail <{}>", user.getId(),
                    submission.getId(), submission.getSubmitterEmail());

            submission.setSubmitterEmail(null);
            submission.setSubmitterName(null);

            if (submission.getSubmitter() != null && !submission.getSubmitter().getId().equals(user.getId())) {
                throw new BadTokenException(String.format(
                        "There is already a submitter <%s> for the submission <%s>, and it isn't the intended user "
                                + "<%s>  Refusing to apply the token for <%s>",
                        submission.getSubmitter(), submission.getId(), user.getId(), token.getReference()));
            }

            submission.setSubmitter(user);
            pass_client.updateObject(submission);
        } else if (user.getId().equals(submission.getSubmitter().getId())) {
            LOG.info("User <{}> already in place as the submitter.  Ignoring user token");
        } else {
            throw new BadTokenException(String.format(
                    "New user token does not match expected e-mail <%s> on submission <%s>; found <%s> instead",
                    token.getReference(), submission.getId(), submission.getSubmitterEmail()));
        }
    }

    private void set_response(HttpServletResponse response, JsonObject obj, HttpStatus status) throws IOException {
        response.getWriter().print(obj.toString());
        response.setStatus(status.value());
    }

    private void set_error_response(HttpServletResponse response, String message, HttpStatus status)
            throws IOException {
        JsonObject obj = Json.createObjectBuilder().add("message", message).build();

        set_response(response, obj, status);
        LOG.error(message);
    }
}
