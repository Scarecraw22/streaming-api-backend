package pl.agh.iet.service.streaming;

import pl.agh.iet.model.CreateStreamRequest;

import java.io.File;

public interface StreamingService {

    String prepareForHlsStreaming(CreateStreamRequest request) throws StreamingServiceException;

    public String getM3u8File(String streamName, String path);

    File getChunk(String streamName, String segmentName, String chunkName);

}
