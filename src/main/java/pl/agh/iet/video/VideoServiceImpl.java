package pl.agh.iet.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.ffmpeg.hls.HlsFFmpegBuilder;
import pl.agh.iet.ffmpeg.hls.HlsPlaylistType;
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

    @Override
    public void encode(Video video) throws VideoServiceException {
        try {
            String filename = video.getName();
            Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                    .resolve(filename);

            Files.createDirectories(videoRootPath);

            String hlsSegmentFilename = videoRootPath
                    .resolve(filename + "_%v")
                    .resolve("data%06d.ts")
                    .toString();
            String hlsMasterFilename = filename + "_master.m3u8";
            Path tmpFile = Files.createTempFile(filename, ".tmp");

            try (OutputStream os = new FileOutputStream(tmpFile.toFile())) {
                IOUtils.copy(video.getContent().getInputStream(), os);
            }

            System.out.println("HlsSegmentFilename: " + hlsSegmentFilename);
            FFmpegBuilder builder = new HlsFFmpegBuilder(new FFmpegBuilder())
                    .setInput(tmpFile.toAbsolutePath().toString())
                    .addOutput(videoRootPath.toAbsolutePath() + filename + "_%v.m3u8")
                    .addExtraArgs("-filter_complex", "[v:0]split=2[vtemp001][vout002];[vtemp001]scale=w=960:h=540[vout001]")
                    .addExtraArgs("-preset", "veryfast")
                    .setGopSize("29.97")
                    .makeSegmentsEqualSized()

                    // For lower quality
                    .addExtraArgs("-map", "[vout001] v:0")
                    .addExtraArgs("-c:v:0", "libx264")
                    .addExtraArgs("-b:v:0", "2000k")
                    // For higher quality
                    .addExtraArgs("-map", "[vout002] v:0")
                    .addExtraArgs("-c:v:1", "libx264")
                    .addExtraArgs("-b:v:1", "6000k")
                    // For audio quality
                    .addExtraArgs("-map", "a:0")
                    .addExtraArgs("-map", "a:0")
                    .setAudioCodec("aac")
                    .setAudioBitRate(128_000)
                    .setAudioChannels(2)

                    .setFormat("hls")
                    .setHlsSegmentDuration(4)
                    .setHlsPlaylistType(HlsPlaylistType.EVENT)
                    .setMasterPlaylistName(hlsMasterFilename)
//                .addExtraArgs("-hls_base_url", ffmpegProperties.getServerUrl())
                    .addExtraArgs("-hls_segment_filename", hlsSegmentFilename)
                    .addExtraArgs("-use_localtime_mkdir", "1")
                    // This tells FFmpeg what streams are combined together. A space seperates each variant and everything that should be placed together
                    // is concatenated with a comma
                    .addExtraArgs("-var_stream_map", "v:0,a:0 v:1,a:1")

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
