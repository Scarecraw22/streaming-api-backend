package pl.agh.iet.service.thumbnail;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

public interface ThumbnailService {

    Path saveThumbnail(MultipartFile thumbnail, String streamName);

    File getThumbnail(String streamName) throws ThumbnailNotExistException;

    File getThumbnailDirForStream(String streamName);
}
