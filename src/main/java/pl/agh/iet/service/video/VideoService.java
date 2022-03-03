package pl.agh.iet.service.video;

import pl.agh.iet.model.GetVideoDetailsListResponse;

public interface VideoService {

    boolean streamExists(String streamName);

    GetVideoDetailsListResponse getVideoDetailsList();

}
