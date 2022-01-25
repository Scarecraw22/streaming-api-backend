package pl.agh.iet.ffmpeg.args;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FormatArg {
    HLS("hls");

    private final String value;
}
