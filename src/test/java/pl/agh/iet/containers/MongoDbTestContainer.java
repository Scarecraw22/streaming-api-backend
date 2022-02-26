package pl.agh.iet.containers;

import com.github.dockerjava.api.model.VolumesFrom;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;
import java.util.Map;

@Slf4j
public class MongoDbTestContainer {

    private static final MongoDBContainer CONTAINER = new MongoDBContainer("mongo:5:0")
            .withEnv(Map.of(
                    "MONGO_DATABASE_USERNAME", "user",
                    "MONGO_DATABASE_PASSWORD", "pass",
//                    "MONGO_INITDB_ROOT_USERNAME", "rootuser",
//                    "MONGO_INITDB_ROOT_PASSWORD", "rootpass",
                    "MONGO_INITDB_DATABASE", "streaming"
            ))
            .withExposedPorts(27017);

    private MongoDbTestContainer() {}

    public synchronized void startWithStopOnShutdown() {
        if (!CONTAINER.isRunning()) {
            CONTAINER.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Stopping mongodb container");
                CONTAINER.stop();
                log.info("Mongodb container stopped");
            }));
        } else {
            log.info("Mongodb container already running");
        }
    }

    public static synchronized MongoDbTestContainer getInstance() {
        return new MongoDbTestContainer();
    }

    public String getUrl() {
//        return CONTAINER.getReplicaSetUrl("streaming");
        return "localhost";
    }

    public String getUsername() {
        return "user";
    }

    public String getPassword() {
        return "pass";
    }

    public String getPort() {
        return "27017";
    }

    public String getDatabase() {
        return "streaming";
    }
}
