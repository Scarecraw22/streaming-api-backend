package pl.agh.iet.video;

import lombok.Value;
import pl.agh.iet.model.Video;

import java.nio.file.Path;

public interface VideoService {

    void encode(Video video) throws VideoServiceException;

    Resolution getResolution(Path path);

    @Value
    class Resolution {
        int width;
        int height;
    }
}
