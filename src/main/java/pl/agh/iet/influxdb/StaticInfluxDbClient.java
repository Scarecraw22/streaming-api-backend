package pl.agh.iet.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.util.function.Consumer;

public class StaticInfluxDbClient {

    public static final InfluxDBClientOptions OPTIONS = InfluxDBClientOptions.builder()
            .url("http://localhost:8086")
            .authenticate("streaming-api-user", "password".toCharArray())
            .bucket("streaming-api")
            .org("AGH")
            .build();

    public static void incrementRequestCounter() {
        writeOperation(client -> {
            WriteApiBlocking writeApi = client.getWriteApiBlocking();
            Point point = Point.measurement("request-counter")
                    .time(System.currentTimeMillis(), WritePrecision.MS)
                    .addField("current", 1);

            writeApi.writePoint(point);
        });
    }

    private static void writeOperation(Consumer<InfluxDBClient> consumer) {
        try (InfluxDBClient client = createClientInstance()) {
            consumer.accept(client);
        }
    }

    public static InfluxDBClient createClientInstance() {
        return InfluxDBClientFactory.create(OPTIONS);
    }
}
