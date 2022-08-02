package pl.agh.iet.influxdb;

import com.influxdb.client.*;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class StaticInfluxDbClient {

    private static final String _VALUE = "_value";
    private static final String CURRENT_COUNTER_QUERY = "from(bucket:\"streaming-api\") "
            + "|> range(start: 0) "
            + "|> filter(fn: (r) => r._measurement == \"request-counter\")"
            + "|> sum()";

    public static final InfluxDBClientOptions OPTIONS = InfluxDBClientOptions.builder()
            .url("http://localhost:8086")
            .authenticate("admin", "password".toCharArray())
            .bucket("streaming-api")
            .org("agh")
            .build();

    public static void incrementRequestCounter() {
        try (InfluxDBClient client = createClientInstance()) {

            WriteApiBlocking writeApi = client.getWriteApiBlocking();

            Point point = Point.measurement("request-counter")
                    .time(System.currentTimeMillis(), WritePrecision.MS)
                    .addField("current", 1);

            writeApi.writePoint(point);

        }
    }

    public static long getCurrentRequestCounter() {
        try (InfluxDBClient client = createClientInstance()) {
            return getCurrentRequestCounter(client);
        }
    }

    private static long getCurrentRequestCounter(InfluxDBClient client) {

        QueryApi queryApi = client.getQueryApi();
        List<FluxTable> result = queryApi.query(CURRENT_COUNTER_QUERY);
        if (!result.isEmpty()) {

            return Optional.ofNullable(result.get(0))
                    .map(FluxTable::getRecords)
                    .orElse(Collections.emptyList())
                    .stream()
                    .findFirst()
                    .map(fluxRecord -> fluxRecord.getValueByKey(_VALUE))
                    .map(Long.class::cast)
                    .orElse(0L);

        } else {
            return 0L;
        }
    }

    public static InfluxDBClient createClientInstance() {
        return InfluxDBClientFactory.create(OPTIONS);
    }
}
