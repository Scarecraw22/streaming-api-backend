package pl.agh.iet.controller;

import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.file.FileUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/streaming-api")
public class StreamingController {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FfmpegProperties ffmpegProperties;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(FileUtils.getFileFromResources("movie.mp4").getAbsolutePath())
                .addOutput(ffmpegProperties.getOutputDir() + "new_file.mp4")
                .setFormat("mp4")
                .setVideoCodec("libx264")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        executor.createJob(builder).run();

        return ResponseEntity.ok("Elo");
    }
}
