package pl.agh.iet.ffmpeg.hls;

import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

@RequiredArgsConstructor
public class HlsFFmpegBuilder {

    private final FFmpegBuilder builder;

    public HlsFFmpegBuilder setInput(String filename) {
        builder.setInput(filename);
        return this;
    }

    public HlsFFmpegOutputBuilder addOutput(String filename) {
        return new HlsFFmpegOutputBuilder(builder.addOutput(filename));
    }

}
