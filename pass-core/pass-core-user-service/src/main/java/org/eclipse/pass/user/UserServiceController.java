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
import org.eclipse.pass.object.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RefreshableElide refreshableElide;

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
            System.err.println("HMMMM " + result.getObjects().size());
            if (result.getObjects().size() > 1) {
                set_error_response(response, "Multiple users matching principal: " + user_name,
                        HttpStatus.INTERNAL_SERVER_ERROR);
                return;
            }

            User user = result.getObjects().get(0);

            String url = PassClient.getUrl(refreshableElide, user);
            JsonObject obj = Json.createObjectBuilder().
                    add("id", user.getId().toString()).
                    add("type", "user").add("uri", url).build();

            set_response(response, obj, HttpStatus.OK);
        }
    }

    private void set_response(HttpServletResponse response, JsonObject obj, HttpStatus status) throws IOException {
        response.getWriter().print(obj.toString());
        response.setStatus(status.value());
    }

    private void set_error_response(HttpServletResponse response, String message,
            HttpStatus status) throws IOException {
        JsonObject obj = Json.createObjectBuilder().add("message", message).build();

        set_response(response, obj, status);
        LOG.error(message);
    }
}
