package pl.agh.iet.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Value
@Builder
@Jacksonized
public class VideoDetails {

    String id;
    String streamName;
    String pathToSources;
    String description;
    BigDecimal duration;
    long initialSize;
    BigDecimal fps;
    ZonedDateTime createdAt;
}
