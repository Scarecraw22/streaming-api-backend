package pl.agh.iet.initializers;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import pl.agh.iet.containers.MongoDbTestContainer;

public class MongoDbTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        MongoDbTestContainer mongoDbContainer = MongoDbTestContainer.getInstance();
        mongoDbContainer.startWithStopOnShutdown();

        TestPropertyValues testPropertyValues = TestPropertyValues.of(
                "spring.data.username", mongoDbContainer.getUsername(),
                "spring.data.password", mongoDbContainer.getPassword(),
                "spring.data.database", mongoDbContainer.getDatabase(),
                "spring.data.port", mongoDbContainer.getPort(),
                "spring.data.host", mongoDbContainer.getUrl()
        );

        testPropertyValues.applyTo(applicationContext.getEnvironment());
    }
}
