package pl.agh.iet.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties
public class FfmpegConfig {

    @Bean
    public FFmpeg ffmpeg(FfmpegProperties properties) {
        log.info("FFmpeg path: {}", properties.getPath());
        try {
            return new FFmpeg(properties.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error while trying to initialize FFmpeg", e);
        }
    }

    @Bean
    public FFprobe ffprobe(FfmpegProperties properties) {
        log.info("FFprobe path: {}", properties.getProbe());
        try {
            return new FFprobe(properties.getProbe());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error while trying to initialize FFprobe", e);
        }
    }
}
