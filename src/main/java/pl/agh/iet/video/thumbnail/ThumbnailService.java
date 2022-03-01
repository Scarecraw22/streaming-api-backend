package pl.agh.iet.video.thumbnail;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface ThumbnailService {

    Path saveThumbnail(MultipartFile thumbnail, String stream);

    Path retrieveThumbnail(String stream);
}
