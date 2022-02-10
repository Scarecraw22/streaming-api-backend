package pl.agh.iet.video.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.web.multipart.MultipartFile;

@Value
@Builder
@Jacksonized
public class Video {

    String name;
    MultipartFile content;
}
