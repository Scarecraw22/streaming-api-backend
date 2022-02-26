package pl.agh.iet.video.thumbnail;

import java.nio.file.Path;

public interface ThumbnailService {

    Path saveThumbnail(Path thumbnail, String stream);

    Path retrieveThumbnail(String stream);
}
