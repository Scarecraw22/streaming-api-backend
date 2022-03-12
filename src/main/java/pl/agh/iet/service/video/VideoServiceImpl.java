package pl.agh.iet.service.video;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agh.iet.db.repository.MetadataRepository;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.model.GetVideoDetailsListResponse;
import pl.agh.iet.service.streaming.hls.HlsMasterLinkCreator;
import pl.agh.iet.service.thumbnail.ThumbnailLinkCreator;

import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final FfmpegProperties ffmpegProperties;
    private final MetadataRepository metadataRepository;
    private final HlsMasterLinkCreator hlsMasterLinkCreator;
    private final ThumbnailLinkCreator thumbnailLinkCreator;

    @Override
    public boolean streamExists(String streamName) {

        boolean streamDirExists = getStreamDir(streamName).exists();
        boolean streamMetadataExists = metadataRepository.findByStreamName(streamName).isPresent();

        return streamDirExists && streamMetadataExists;
    }

    @Override
    public File getStreamDir(String streamName) {
        return Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName)
                .toFile();
    }

    @Override
    public GetVideoDetailsListResponse getVideoDetailsList() {
        return GetVideoDetailsListResponse.builder()
                .detailsList(metadataRepository.findAll().stream()
                        .map(entity -> GetVideoDetailsListResponse.Video.builder()
                                .id(entity.getId())
                                .streamName(entity.getStreamName())
                                .description(entity.getDescription())
                                .title(entity.getTitle())
                                .masterLink(hlsMasterLinkCreator.createMasterLink(entity.getStreamName()))
                                .thumbnailLink(thumbnailLinkCreator.createThumbnailLink(entity.getStreamName()))
                                .createdAt(entity.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
