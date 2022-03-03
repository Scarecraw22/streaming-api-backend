package pl.agh.iet.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.ZonedDateTime;
import java.util.List;

@Value
@Builder
@Jacksonized
public class GetVideoDetailsListResponse {

    List<Video> detailsList;

    @Value
    @Builder
    @Jacksonized
    public static class Video {
        String id;
        String streamName;
        String thumbnailFilename;
        String description;
        ZonedDateTime createdAt;
    }
}
