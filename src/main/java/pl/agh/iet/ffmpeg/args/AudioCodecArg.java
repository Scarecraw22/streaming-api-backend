package pl.agh.iet.ffmpeg.args;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AudioCodecArg {
    AAC("aac");

    private final String value;
}
