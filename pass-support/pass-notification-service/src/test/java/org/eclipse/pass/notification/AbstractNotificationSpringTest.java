package org.eclipse.pass.notification;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@TestPropertySource(properties = {
    "pass.notification.configuration=classpath:test-notification.json"
})
public abstract class AbstractNotificationSpringTest {

    static {
        System.setProperty("pass.core.url", "localhost:8080");
        System.setProperty("pass.core.user", "user");
        System.setProperty("pass.core.password", "moo");
    }

}
