package pl.agh.iet.video.hls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.agh.iet.ffmpeg.FfmpegProperties;
import pl.agh.iet.utils.HlsConsts;
import pl.agh.iet.utils.StringConsts;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class HlsFilesNamingServiceImpl implements HlsFilesNamingService {

    private final FfmpegProperties ffmpegProperties;

    @Override
    public String createHlsSegmentName(String baseName) {
        Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                .resolve(baseName);

        String hlsSegmentName = videoRootPath
                .resolve(baseName + "_%v")
                .resolve("data%06d.ts")
                .toString();

        log.info("Created HlsSegmentName: {}", hlsSegmentName);

        return hlsSegmentName;
    }

    @Override
    public String createHlsMasterFilename(String baseName) {
        String masterFilename = baseName + StringConsts.UNDERSCORE + HlsConsts.MASTER_M3U8;
        log.info("Created HLS master filename: {}", masterFilename);

        return masterFilename;
    }

    @Override
    public String createHlsOutputPattern(String baseName) {
        Path videoRootPath = Paths.get(ffmpegProperties.getOutputDir())
                .resolve(baseName);
        String hlsOutputPattern = videoRootPath.toAbsolutePath().resolve(baseName) + "_%v.m3u8";

        log.info("Created HLS output pattern: {}", hlsOutputPattern);
        return hlsOutputPattern;
    }
}
