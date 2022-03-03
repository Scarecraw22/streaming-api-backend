package pl.agh.iet.service.streaming.quality;

import lombok.extern.slf4j.Slf4j;
import pl.agh.iet.service.streaming.metadata.Metadata;

import java.util.Collection;
import java.util.LinkedList;

@Slf4j
public enum HighestQualityStrategy {
    HIGHEST_WIDTH {
        @Override
        public Collection<Quality> getQualitiesFromHighest(Metadata.Resolution resolution) {
            LinkedList<Quality> qualitiesFromHighest = new LinkedList<>(Quality.QUALITIES_FROM_HIGHEST);

            for (Quality actualQuality : Quality.QUALITIES_FROM_HIGHEST) {
                if (resolution.getWidth() < actualQuality.getWidth()) {
                    qualitiesFromHighest.removeFirst();
                }
            }

            if (qualitiesFromHighest.isEmpty()) {
                qualitiesFromHighest.addFirst(Quality.LOW);
            }

            log.info("Highest Quality: {}", qualitiesFromHighest.getFirst());
            return qualitiesFromHighest;
        }
    };

    public abstract Collection<Quality> getQualitiesFromHighest(Metadata.Resolution resolution);
}
