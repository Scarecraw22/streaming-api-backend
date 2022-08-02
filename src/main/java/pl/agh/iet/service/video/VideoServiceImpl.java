package pl.agh.iet.service.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.agh.iet.db.MetadataEntity;
import pl.agh.iet.db.repository.MetadataRepository;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.model.GetVideoDetailsListResponse;
import pl.agh.iet.model.SearchVideoRequest;
import pl.agh.iet.service.streaming.hls.HlsMasterLinkCreator;
import pl.agh.iet.service.thumbnail.ThumbnailLinkCreator;

import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final FfmpegProperties ffmpegProperties;
    private final MetadataRepository metadataRepository;
    private final HlsMasterLinkCreator hlsMasterLinkCreator;
    private final ThumbnailLinkCreator thumbnailLinkCreator;
    private final MongoTemplate mongoTemplate;

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

        log.info("Get all streams...");
        var response = GetVideoDetailsListResponse.builder()
                .detailsList(metadataRepository.findAll().stream()
                        .map(this::convertToVideoResponse)
                        .collect(Collectors.toList()))
                .build();

        log.info("Found: {} streams", response.getDetailsList().size());

        return response;
    }

    @Override
    public GetVideoDetailsListResponse filterStreams(SearchVideoRequest request) {

        log.info("Starting to search streams with properties: {}", request);

        Query query = new Query();
        Pattern pattern = Pattern.compile(".*" + request.getTitle().trim() + ".*", Pattern.CASE_INSENSITIVE);
        query.addCriteria(Criteria.where("title").regex(pattern));
        var response = GetVideoDetailsListResponse.builder()
                .detailsList(mongoTemplate.find(query, MetadataEntity.class)
                        .stream()
                        .map(this::convertToVideoResponse)
                        .collect(Collectors.toList()))
                .build();

        log.info("Found {} matching streams", response.getDetailsList().size());

        return response;
    }

    private GetVideoDetailsListResponse.Video convertToVideoResponse(MetadataEntity entity) {
        return GetVideoDetailsListResponse.Video.builder()
                .id(entity.getId())
                .streamName(entity.getStreamName())
                .description(entity.getDescription())
                .title(entity.getTitle())
                .masterLink(hlsMasterLinkCreator.createMasterLink(entity.getStreamName()))
                .thumbnailLink(thumbnailLinkCreator.createThumbnailLink(entity.getStreamName()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
