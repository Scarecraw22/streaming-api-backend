package pl.agh.iet.video;

import pl.agh.iet.model.Video;

public interface VideoService {

    void encode(Video video) throws VideoServiceException;
}
