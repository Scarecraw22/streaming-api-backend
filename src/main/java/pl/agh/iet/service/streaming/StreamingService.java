package pl.agh.iet.service.streaming;

import pl.agh.iet.model.AddVideoRequest;

import java.io.File;

public interface StreamingService {

    String addVideo(AddVideoRequest request) throws StreamingServiceException;

    String getM3u8File(String streamName, String path);

    File getChunk(String streamName, String segmentName, String chunkName);

    String deleteStreamById(String id);
}
