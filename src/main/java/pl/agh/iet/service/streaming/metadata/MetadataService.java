package pl.agh.iet.service.streaming.metadata;

import pl.agh.iet.service.streaming.quality.Quality;

import java.nio.file.Path;
import java.util.Collection;

public interface MetadataService {

    /**
     * Retrieves {@link Metadata} from given video file
     *
     * @param path Video file
     * @return {@link Metadata} of given {@link Path}
     */
    Metadata getMetadata(Path path);

    /**
     * Retrieves {@link Collection} of {@link Quality} starting from the highest possible {@link Quality} based on given {@link Metadata.Resolution}
     *
     * @param resolution {@link Metadata.Resolution}
     * @return {@link Collection} of {@link Quality} starting from the highest possible {@link Quality}
     */
    Collection<Quality> getQualitiesFromHighest(Metadata.Resolution resolution);
}
