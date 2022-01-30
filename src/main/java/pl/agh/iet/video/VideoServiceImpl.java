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
import pl.agh.iet.file.M3U8FileEditor;
import pl.agh.iet.utils.FileUtils;
import pl.agh.iet.utils.HlsConsts;
import pl.agh.iet.utils.StringConsts;
import pl.agh.iet.video.hls.HlsFilesNamingService;
import pl.agh.iet.video.metadata.Metadata;
import pl.agh.iet.video.metadata.MetadataService;
import pl.agh.iet.video.model.Video;
import pl.agh.iet.video.quality.Quality;

import java.io.File;
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
    private final M3U8FileEditor m3u8FileEditor;

    @Override
    public void prepareForHlsStreaming(Video video) throws VideoServiceException {
        try {
            String streamName = video.getName();
            Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                    .resolve(streamName);

            Files.createDirectories(videoRootPath);

            String hlsSegmentFilename = hlsFilesNamingService.createHlsSegmentName(streamName);

            String hlsMasterFilename = hlsFilesNamingService.createHlsMasterFilename(streamName);
            String outputPattern = hlsFilesNamingService.createHlsOutputPattern(streamName);
            Path tmpFile = Files.createTempFile(streamName, FileUtils.TMP_EXTENSION);

            try (OutputStream os = new FileOutputStream(tmpFile.toFile())) {
                IOUtils.copy(video.getContent().getInputStream(), os);
            }
            Metadata metadata = metadataService.getMetadata(tmpFile);

            log.info("Retrieved metadata: {}", metadata);

            Collection<Quality> qualitiesFromHighest = metadataService.getQualitiesFromHighest(metadata.getResolution());
            String fps = metadata.getFps().toString();

            FfmpegBuilderCreator.Config builderCreatorConfig = FfmpegBuilderCreator.Config.builder()
                    .baseName(streamName)
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
                    .build();

            log.info("Creating FFmpeg filter based on given config: {}", builderCreatorConfig);
            FFmpegBuilder builder = ffmpegBuilderCreator.createFfmpegBuilder(builderCreatorConfig);

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            executor.createJob(builder).run();

            if (tmpFile.toFile().delete()) {
                log.info("Tmp file deleted");
            }

            m3u8FileEditor.setFileContent(streamName);

        } catch (IOException e) {
            throw new VideoServiceException("Error while trying to encode video with name: " + video.getName(), e);
        }
    }

    @Override
    public String getMasterFileContent(String streamName) {
        log.info("Searching {} file for stream: {}", HlsConsts.MASTER_M3U8, streamName);

        String masterFilename = streamName + StringConsts.UNDERSCORE + HlsConsts.MASTER_M3U8;
        String content = FileUtils.getFileContent(Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName).resolve(masterFilename).toString());

        log.info("Found {} file for stream: {}. Content: {}", HlsConsts.MASTER_M3U8, streamName, content);

        return content;
    }

    @Override
    public String getSegmentMasterFileContent(String streamName, String segmentName) {
        log.info("Searching {} file for stream: {}", segmentName, streamName);

        String content = FileUtils.getFileContent(Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName).resolve(segmentName).toString());

        log.info("Found {} file for stream: {}. Content: {}", segmentName, streamName, content);

        return content;
    }

    @Override
    public File getChunk(String streamName, String segmentName, String chunkName) {
        log.info("Searching for chunk: {}/{}/{}", streamName, segmentName, chunkName);

        File file = FileUtils.getFile(Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName).resolve(segmentName).resolve(chunkName).toString());

        log.info("Found chunk: {}/{}/{}", streamName, segmentName, chunkName);
        return file;
    }
}
