package pl.agh.iet.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.ffmpeg.hls.HlsFFmpegBuilder;
import pl.agh.iet.ffmpeg.hls.HlsPlaylistType;
import pl.agh.iet.hls.HlsFilesNamingService;
import pl.agh.iet.model.Video;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FfmpegProperties ffmpegProperties;
    private final HlsFilesNamingService hlsFilesNamingService;

    @Override
    public void encode(Video video) throws VideoServiceException {
//        oldWay(video);
        newWay(video);
    }

    @Override
    public Resolution getResolution(Path path) {

        try {
            FFmpegProbeResult probeResult = ffprobe.probe(path.toAbsolutePath().toString());
            FFmpegStream stream = probeResult.getStreams().get(0);

            return new VideoService.Resolution(stream.width, stream.height);

        } catch (IOException e) {
            throw new VideoServiceException("Error while trying to get info from video: " + path.toAbsolutePath(), e);
        }
    }

    private void oldWay(Video video) {
        try {
            String filename = video.getName();
            Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                    .resolve(filename);

            Files.createDirectories(videoRootPath);

            String hlsSegmentFilename = videoRootPath
                    .resolve(filename + "_%v")
                    .resolve("data%06d.ts")
                    .toString();

            String hlsMasterFilename = filename + "_" + "master.m3u8";
            Path tmpFile = Files.createTempFile(filename, ".tmp");

            try (OutputStream os = new FileOutputStream(tmpFile.toFile())) {
                IOUtils.copy(video.getContent().getInputStream(), os);
            }

            System.out.println("HlsSegmentFilename: " + hlsSegmentFilename);
            FFmpegBuilder builder = new HlsFFmpegBuilder(new FFmpegBuilder())
                    .setInput(tmpFile.toAbsolutePath().toString())
                    .addOutput(videoRootPath.toAbsolutePath().resolve(filename) + "_%v.m3u8")
                    .addExtraArgs("-filter_complex", "[v:0]split=3[v1][v2][v3];[v1]copy[v1out];[v2]scale=w=960:h=540[v2out];[v3]scale=w=640:h=360[v3out]")
                    .addExtraArgs("-preset", "veryfast")
                    .setGopSize("29.97")
                    .makeSegmentsEqualSized()

                    // For higher quality
                    .addExtraArgs("-map", "[v1out] v:0")
                    .addExtraArgs("-c:v:0", "libx264")
                    .addExtraArgs("-b:v:0", "6000k")
                    // For 960x540
                    .addExtraArgs("-map", "[v2out] v:0")
                    .addExtraArgs("-c:v:1", "libx264")
                    .addExtraArgs("-b:v:1", "1500k")
                    // For 640x360
                    .addExtraArgs("-map", "[v3out] v:0")
                    .addExtraArgs("-c:v:2", "libx264")
                    .addExtraArgs("-b:v:2", "1000k")

                    // For audio quality

                    .addExtraArgs("-map", "a:0")
                    .addExtraArgs("-map", "a:0")
                    .addExtraArgs("-map", "a:0")
                    .setAudioCodec("aac")
                    .setAudioBitRate(128_000)
                    .setAudioChannels(2)

                    .setFormat("hls")
                    .setHlsSegmentDuration(4)
                    .setHlsPlaylistType(HlsPlaylistType.VOD)
                    .setMasterPlaylistName(hlsMasterFilename)
//                .addExtraArgs("-hls_base_url", ffmpegProperties.getServerUrl())
                    .addExtraArgs("-hls_segment_filename", hlsSegmentFilename)
                    .addExtraArgs("-hls_base_url", "http://localhost:8080/streaming-api/" + filename)
                    .addExtraArgs("-use_localtime_mkdir", "1")
                    // This tells FFmpeg what streams are combined together. A space seperates each variant and everything that should be placed together
                    // is concatenated with a comma
                    .addExtraArgs("-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2")

                    .done();

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

    private void newWay(Video video) {
        try {
            String baseName = video.getName();
            Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                    .resolve(baseName);

            Files.createDirectories(videoRootPath);

            String hlsSegmentFilename = hlsFilesNamingService.createHlsSegmentName(baseName);

            String hlsMasterFilename = hlsFilesNamingService.createHlsMasterFilename(baseName);
            String outputPattern = hlsFilesNamingService.createHlsOutputPattern(baseName);
            Path tmpFile = Files.createTempFile(baseName, ".tmp");

            try (OutputStream os = new FileOutputStream(tmpFile.toFile())) {
                IOUtils.copy(video.getContent().getInputStream(), os);
            }
            log.info("Getting resolution from video: {}", baseName);
            VideoService.Resolution resolution = getResolution(tmpFile);
            log.info("Retrieved resolution: {}", resolution);

            FFmpegBuilder builder = new HlsFFmpegBuilder(new FFmpegBuilder())
                    .setInput(tmpFile.toAbsolutePath().toString())
                    .addOutput(outputPattern)
                    .addExtraArgs("-filter_complex", "[v:0]split=3[v1][v2][v3];[v1]copy[v1out];[v2]scale=w=960:h=540[v2out];[v3]scale=w=640:h=360[v3out]")
                    .addExtraArgs("-preset", "veryfast")
                    .setGopSize("29.97")
                    .makeSegmentsEqualSized()

                    // For higher quality
                    .addExtraArgs("-map", "[v1out] v:0")
                    .addExtraArgs("-c:v:0", "libx264")
                    .addExtraArgs("-b:v:0", "6000k")
                    // For 960x540
                    .addExtraArgs("-map", "[v2out] v:0")
                    .addExtraArgs("-c:v:1", "libx264")
                    .addExtraArgs("-b:v:1", "1500k")
                    // For 640x360
                    .addExtraArgs("-map", "[v3out] v:0")
                    .addExtraArgs("-c:v:2", "libx264")
                    .addExtraArgs("-b:v:2", "1000k")

                    // For audio quality

                    .addExtraArgs("-map", "a:0")
                    .addExtraArgs("-map", "a:0")
                    .addExtraArgs("-map", "a:0")
                    .setAudioCodec("aac")
                    .setAudioBitRate(128_000)
                    .setAudioChannels(2)

                    .setFormat("hls")
                    .setHlsSegmentDuration(4)
                    .setHlsPlaylistType(HlsPlaylistType.VOD)
                    .setMasterPlaylistName(hlsMasterFilename)
//                .addExtraArgs("-hls_base_url", ffmpegProperties.getServerUrl())
                    .addExtraArgs("-hls_segment_filename", hlsSegmentFilename)
                    .addExtraArgs("-hls_base_url", "http://localhost:8080/streaming-api/" + baseName)
                    .addExtraArgs("-use_localtime_mkdir", "1")
                    // This tells FFmpeg what streams are combined together. A space seperates each variant and everything that should be placed together
                    // is concatenated with a comma
                    .addExtraArgs("-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2")

                    .done();

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
