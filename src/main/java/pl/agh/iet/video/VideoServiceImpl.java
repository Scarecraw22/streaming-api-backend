package pl.agh.iet.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import pl.agh.iet.ffmpeg.FfmpegBuilderCreator;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.ffmpeg.args.PresetArg;
import pl.agh.iet.ffmpeg.args.VideoCodecArg;
import pl.agh.iet.ffmpeg.hls.HlsPlaylistType;
import pl.agh.iet.utils.FileUtils;
import pl.agh.iet.video.hls.HlsFilesNamingService;
import pl.agh.iet.video.metadata.Metadata;
import pl.agh.iet.video.metadata.MetadataService;
import pl.agh.iet.video.model.Video;
import pl.agh.iet.video.quality.Quality;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FfmpegProperties ffmpegProperties;
    private final HlsFilesNamingService hlsFilesNamingService;
    private final MetadataService metadataService;
    private final FfmpegBuilderCreator ffmpegBuilderCreator;

    @Override
    public void prepareForHlsStreaming(Video video) throws VideoServiceException {
        try {
            String baseName = video.getName();
            Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                    .resolve(baseName);

            Files.createDirectories(videoRootPath);

            String hlsSegmentFilename = hlsFilesNamingService.createHlsSegmentName(baseName);

            String hlsMasterFilename = hlsFilesNamingService.createHlsMasterFilename(baseName);
            String outputPattern = hlsFilesNamingService.createHlsOutputPattern(baseName);
            Path tmpFile = Files.createTempFile(baseName, FileUtils.TMP_EXTENSION);

            try (OutputStream os = new FileOutputStream(tmpFile.toFile())) {
                IOUtils.copy(video.getContent().getInputStream(), os);
            }
            Metadata metadata = metadataService.getMetadata(tmpFile);

            log.info("Retrieved metadata: {}", metadata);

            Collection<Quality> qualitiesFromHighest = metadataService.getQualitiesFromHighest(metadata.getResolution());
            String fps = metadata.getFps().toString();

            FfmpegBuilderCreator.Config builderCreatorConfig = FfmpegBuilderCreator.Config.builder()
                    .baseName(baseName)
                    .input(tmpFile)
                    .outputPattern(outputPattern)
                    .preset(PresetArg.VERY_FAST)
                    .fps(fps)
                    .qualitiesFromHighest(qualitiesFromHighest)
                    .videoCodec(VideoCodecArg.LIBX_264)
                    .hlsSegmentDuration(4)
                    .hlsPlaylistType(HlsPlaylistType.VOD)
                    .hlsMasterFilename(hlsMasterFilename)
                    .hlsSegmentFilename(hlsSegmentFilename)
                    .hlsBaseUrl(ffmpegProperties.getServerUrl())
                    .build();

            log.info("Creating FFmpeg filter based on given config: {}", builderCreatorConfig);
            FFmpegBuilder builder = ffmpegBuilderCreator.createFfmpegBuilder(builderCreatorConfig);

            // Replace absolute paths in m3u8 files or find option in FFmpeg to do this for me

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            executor.createJob(builder).run();

            if (tmpFile.toFile().delete()) {
                log.info("Tmp file deleted");
            }

        } catch (IOException e) {
            throw new VideoServiceException("Error while trying to encode video with name: " + video.getName(), e);
        }
    }
}
