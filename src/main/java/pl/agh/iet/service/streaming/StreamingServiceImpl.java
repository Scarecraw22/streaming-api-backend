package pl.agh.iet.service.streaming;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;
import pl.agh.iet.db.MetadataEntity;
import pl.agh.iet.db.repository.MetadataRepository;
import pl.agh.iet.ffmpeg.FfmpegBuilderCreator;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.ffmpeg.args.PresetArg;
import pl.agh.iet.ffmpeg.args.VideoCodecArg;
import pl.agh.iet.ffmpeg.hls.HlsPlaylistType;
import pl.agh.iet.ffmpeg.hls.HlsSegmentType;
import pl.agh.iet.file.M3U8FileEditor;
import pl.agh.iet.model.CreateStreamRequest;
import pl.agh.iet.service.streaming.hls.HlsFilesNamingService;
import pl.agh.iet.service.streaming.metadata.Metadata;
import pl.agh.iet.service.streaming.metadata.MetadataService;
import pl.agh.iet.service.streaming.quality.Quality;
import pl.agh.iet.service.thumbnail.ThumbnailService;
import pl.agh.iet.service.video.VideoService;
import pl.agh.iet.utils.FileUtils;
import pl.agh.iet.utils.HlsConsts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FfmpegProperties ffmpegProperties;
    private final HlsFilesNamingService hlsFilesNamingService;
    private final MetadataService metadataService;
    private final FfmpegBuilderCreator ffmpegBuilderCreator;
    private final M3U8FileEditor m3u8FileEditor;
    private final MetadataRepository metadataRepository;
    private final ThumbnailService thumbnailService;
    private final VideoService videoService;

    @Override
    public String createStream(CreateStreamRequest request) throws StreamingServiceException {
        try {
            String streamName = request.getName();
            Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                    .resolve(streamName);

            Files.createDirectories(videoRootPath);

            String hlsSegmentFilename = hlsFilesNamingService.createHlsSegmentName(streamName);

            String hlsMasterFilename = hlsFilesNamingService.createHlsMasterFilename(streamName);
            String outputPattern = hlsFilesNamingService.createHlsOutputPattern(streamName);
            Path tmpFile = Files.createTempFile(streamName, FileUtils.TMP_EXTENSION);

            FileUtils.copyFile(request.getContent(), tmpFile);

            Metadata metadata = metadataService.getMetadata(tmpFile);
            MetadataEntity metadataEntity = new MetadataEntity();
            metadataEntity.setStreamName(streamName);
            metadataEntity.setDescription(request.getDescription());
            metadataEntity.setFps(metadata.getFps());
            metadataEntity.setInitialSize(tmpFile.toFile().length());
            metadataEntity.setDuration(metadata.getDuration());
            metadataEntity.setTitle(request.getTitle());

            log.info("Retrieved metadata: {}", metadata);

            Collection<Quality> qualitiesFromHighest = metadataService.getQualitiesFromHighest(metadata.getResolution());
            String fps = metadata.getFps().toString();

            FfmpegBuilderCreator.Config builderCreatorConfig = FfmpegBuilderCreator.Config.builder()
                    .baseName(streamName)
                    .input(tmpFile)
                    .outputPattern(outputPattern)
                    .preset(PresetArg.SLOW)
                    .fps(fps)
                    .qualitiesFromHighest(qualitiesFromHighest)
                    .videoCodec(VideoCodecArg.LIBX_264)
                    .hlsSegmentDuration(4)
                    .hlsPlaylistType(HlsPlaylistType.VOD)
                    .hlsMasterFilename(hlsMasterFilename)
                    .hlsSegmentFilename(hlsSegmentFilename)
                    .hlsSegmentType(HlsSegmentType.MPEGTS.getValue())
                    .build();

            log.info("Creating FFmpeg filter based on given config: {}", builderCreatorConfig);
            FFmpegBuilder builder = ffmpegBuilderCreator.createFfmpegBuilder(builderCreatorConfig);

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            executor.createJob(builder).run();

            if (tmpFile.toFile().delete()) {
                log.info("Tmp file deleted");
            }

            m3u8FileEditor.setFileContent(streamName, qualitiesFromHighest);

            metadataEntity.setCreatedAt(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")));
            Path thumbnailPath = thumbnailService.saveThumbnail(request.getThumbnail(), streamName);

            metadataEntity.setThumbnailFilename(thumbnailPath.toFile().getName());
            MetadataEntity savedEntity = metadataRepository.save(metadataEntity);

            return savedEntity.getId();

        } catch (IOException e) {
            throw new StreamingServiceException("Error while trying to encode video with name: " + request.getName(), e);
        }
    }

    @Override
    public String getM3u8File(String streamName, String path) {
        log.info("Searching {} file for stream: {}", HlsConsts.MASTER_M3U8, streamName);

        String content = FileUtils.getFileContent(Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName).resolve(path).toString());

        log.info("Found {} file for stream: {}", path, streamName);

        return content;
    }

    @Override
    public File getChunk(String streamName, String segmentName, String chunkName) {
        log.info("Searching for chunk: {}/{}/{}", streamName, segmentName, chunkName);

        File file = FileUtils.getFile(Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName).resolve(segmentName).resolve(chunkName).toString());

        log.info("Found chunk: {}/{}/{}", streamName, segmentName, chunkName);
        return file;
    }

    @Override
    public String deleteStreamById(String id) {

        log.info("Starting delete process of stream with id: {}", id);

        MetadataEntity metadataEntity = metadataRepository.findById(id)
                .orElseThrow(() -> new StreamNotExistException("Stream with id: " + id + " not exists"));
        String streamName = metadataEntity.getStreamName();

        File streamDir = videoService.getStreamDir(streamName);

        log.info("Deleting metadata for stream: {}", streamName);
        metadataRepository.deleteById(metadataEntity.getId());

        log.info("Deleting stream directory for stream: {}", streamName);
        if (streamDir.exists()) {
            FileUtils.deleteNonEmptyDir(streamDir);
        } else {
            log.warn("Stream directory for stream: {} not exists", streamName);
        }

        log.info("Deleting thumbnail for stream: {}", streamName);
        File thumbnailDir = thumbnailService.getThumbnailDirForStream(streamName);

        if (thumbnailDir.exists()) {
            FileUtils.deleteNonEmptyDir(thumbnailDir);
        } else {
            log.warn("Thumbnail for stream: {} not exists", streamName);
        }

        log.info("Delete process of stream: {} completed", streamName);

        return metadataEntity.getId();
    }
}
