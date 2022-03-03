package pl.agh.iet.service.video;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agh.iet.db.repository.MetadataRepository;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.model.GetVideoDetailsListResponse;

import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final FfmpegProperties ffmpegProperties;
    private final MetadataRepository metadataRepository;

    @Override
    public boolean streamExists(String streamName) {

        return Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName)
                .toFile()
                .exists();
    }

    @Override
    public GetVideoDetailsListResponse getVideoDetailsList() {
        return GetVideoDetailsListResponse.builder()
                .detailsList(metadataRepository.findAll().stream()
                        .map(entity -> GetVideoDetailsListResponse.Video.builder()
                                .id(entity.getId())
                                .streamName(entity.getStreamName())
                                .description(entity.getDescription())
                                .thumbnailFilename(entity.getThumbnailFilename())
                                .createdAt(entity.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();

    }
}
