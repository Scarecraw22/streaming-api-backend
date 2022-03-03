package pl.agh.iet.service.streaming.metadata;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class Metadata {

    BigDecimal fps;
    Resolution resolution;
    BigDecimal duration;

    @Value
    @Builder
    public static class Resolution {
        int width;
        int height;
    }
}
