package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.ffmpeg.hls.HlsFFmpegBuilder;
import pl.agh.iet.ffmpeg.hls.HlsPlaylistType;
import pl.agh.iet.file.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/streaming-api")
public class StreamingController {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FfmpegProperties ffmpegProperties;

    @GetMapping("/test/{videoName}")
    public ResponseEntity<String> test(@PathVariable String videoName) {
        try {
            Files.createDirectories(Paths.get(ffmpegProperties.getOutputDir()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String hlsSegmentFilename = Paths.get(ffmpegProperties.getOutputDir())
                .resolve(videoName + "_%v")
                .resolve("data%06d.ts")
                .toString();
        System.out.println("HlsSegmentFilename: " + hlsSegmentFilename);
        FFmpegBuilder builder = new HlsFFmpegBuilder(new FFmpegBuilder())
                .setInput(FileUtils.getFileFromResources("movie.mp4").getAbsolutePath())
                .addOutput(ffmpegProperties.getOutputDir() + videoName + "_%v.m3u8")
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
                .setMasterPlaylistName("master.m3u8")
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

        return ResponseEntity.ok("Elo");
    }

    @GetMapping("/{streamName}/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String streamName, @PathVariable String filename) {

        try {
            File file = FileUtils.getFileFromResources(Paths.get(streamName).resolve(filename).toString());

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.ok()
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ResponseEntity.internalServerError().build();
    }
}
