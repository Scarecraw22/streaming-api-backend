package pl.agh.iet.ffmpeg.args;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VideoCodecArg {
    LIBX_264("libx264");

    private final String value;
}
