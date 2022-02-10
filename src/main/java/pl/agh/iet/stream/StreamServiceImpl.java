package pl.agh.iet.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agh.iet.ffmpeg.FfmpegProperties;

import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StreamServiceImpl implements StreamService {

    private final FfmpegProperties ffmpegProperties;

    @Override
    public boolean streamExists(String streamName) {

        return Paths.get(ffmpegProperties.getOutputDir()).resolve(streamName)
                .toFile()
                .exists();
    }
}
