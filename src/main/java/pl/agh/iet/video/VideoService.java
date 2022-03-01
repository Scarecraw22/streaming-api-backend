package pl.agh.iet.video;

import pl.agh.iet.model.CreateStreamRequest;

import java.io.File;

public interface VideoService {

    String prepareForHlsStreaming(CreateStreamRequest request) throws VideoServiceException;

    public String getM3u8File(String streamName, String path);

    File getChunk(String streamName, String segmentName, String chunkName);

}
