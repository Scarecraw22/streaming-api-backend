package pl.agh.iet.video;

import pl.agh.iet.video.model.Video;

public interface VideoService {

    /**
     * Prepares {@link Video} for HLS streaming.
     * It takes {@link Video} object and splits it into multiple videos
     * with different resolutions. Then this method chunks each video to
     * smaller video files.
     * Creates m3u8 files to support HLS streaming protocol.
     *
     * @param video {@link Video}
     * @throws VideoServiceException thrown when something went wrong while preparing video files
     */
    void prepareForHlsStreaming(Video video) throws VideoServiceException;
}
