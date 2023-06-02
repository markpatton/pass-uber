package org.eclipse.pass.notification;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@TestPropertySource(properties = {
    "pass.notification.mode=DEMO",
    "pass.notification.configuration=classpath:it-notification.json"
})
public abstract class AbstractNotificationSpringIntegrationTest {

    static {
        System.setProperty("pass.core.url", "http://localhost:8080");
        System.setProperty("pass.core.user", "backend");
        System.setProperty("pass.core.password", "backend");
    }

}
