package pl.agh.iet.ffmpeg.args;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A preset is a collection of options that will provide a certain encoding speed to compression ratio.
 * A slower preset will provide better compression (compression is quality per filesize).
 * This means that, for example, if you target a certain file size or constant bit rate,
 * you will achieve better quality with a slower preset.
 * Similarly, for constant quality encoding, you will simply save bitrate by choosing a slower preset.
 */
@Getter
@RequiredArgsConstructor
public enum PresetArg {
    ULTRAFAST("ultrafast"),
    SUPER_FAST("superfast"),
    VERY_FAST("veryfast"),
    FASTER("faster"),
    FAST("fast"),
    MEDIUM("medium"),
    SLOW("slow"),
    SLOWER("slower"),
    VERY_SLOW("veryslow");

    private final String value;
}
