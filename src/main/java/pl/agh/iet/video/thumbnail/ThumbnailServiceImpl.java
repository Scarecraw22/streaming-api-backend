package pl.agh.iet.video.thumbnail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.agh.iet.utils.FileUtils;
import pl.agh.iet.utils.StringConsts;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailServiceImpl implements ThumbnailService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpeg",
            "jpg",
            "png"
    );

    private final ThumbnailProperties thumbnailProperties;

    @Override
    public Path saveThumbnail(Path thumbnail, String stream) {

        String extension = FileUtils.getExtension(thumbnail.toFile().getName())
                .orElse(null);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            log.info("Forbidden extension: {}, of image: {}", extension, stream);
            throw new ForbiddenThumbnailExtensionException("Forbidden extension: " + extension + " for given thumbnail: {}" + thumbnail + " for stream: {}" + stream);
        }

        log.info("Trying to save thumbnail for stream: {}", stream);

        Path thumbnailPath = Paths.get(thumbnailProperties.getPath())
                .resolve(stream)
                .resolve(stream + StringConsts.UNDERSCORE + "thumbnail" + StringConsts.DOT + extension);

        FileUtils.copyFile(thumbnail, thumbnailPath);

        return thumbnailPath;
    }

    @Override
    public Path retrieveThumbnail(String stream) {
        return null;
    }

}
