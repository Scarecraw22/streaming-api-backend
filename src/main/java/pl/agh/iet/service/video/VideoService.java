package pl.agh.iet.service.video;

import pl.agh.iet.model.GetVideoDetailsListResponse;
import pl.agh.iet.model.SearchVideoRequest;

import java.io.File;

public interface VideoService {

    boolean streamExists(String streamName);

    GetVideoDetailsListResponse getVideoDetailsList();

    File getStreamDir(String streamName);

    GetVideoDetailsListResponse filterStreams(SearchVideoRequest request);
}
