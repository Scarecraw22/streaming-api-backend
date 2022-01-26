package pl.agh.iet.video.quality

import pl.agh.iet.video.metadata.Metadata
import spock.lang.Specification
import spock.lang.Unroll

class HighestQualityStrategyTest extends Specification {

    private static Collection<Quality> _4K_HIGHEST = Quality.QUALITIES_FROM_HIGHEST
    private static Collection<Quality> HD_1080_HIGHEST = List.of(
            Quality.HD_1080,
            Quality.HD_720,
            Quality.HIGH,
            Quality.MED,
            Quality.LOW
    )
    private static Collection<Quality> HD_720_HIGHEST = List.of(
            Quality.HD_720,
            Quality.HIGH,
            Quality.MED,
            Quality.LOW
    )
    private static Collection<Quality> HIGH_HIGHEST = List.of(
            Quality.HIGH,
            Quality.MED,
            Quality.LOW
    )
    private static Collection<Quality> MED_HIGHEST = List.of(
            Quality.MED,
            Quality.LOW
    )
    private static Collection<Quality> LOW_HIGHEST = List.of(
            Quality.LOW
    )

    @Unroll
    def "HIGHEST_WIDTH when width=#width, height=#height should return #expectedQuality"() {
        given:
        def resolution = Metadata.Resolution.builder()
                .width(width)
                .height(height)
                .build()

        when:
        def result = HighestQualityStrategy.HIGHEST_WIDTH.getQualitiesFromHighest(resolution)

        then:
        result == expectedQuality

        where:
        width | height || expectedQuality
        3841  | 2100   || _4K_HIGHEST
        3840  | 2160   || _4K_HIGHEST
        2000  | 1200   || HD_1080_HIGHEST
        1920  | 1080   || HD_1080_HIGHEST
        1500  | 800    || HD_720_HIGHEST
        1280  | 720    || HD_720_HIGHEST
        1000  | 600    || HIGH_HIGHEST
        960   | 540    || HIGH_HIGHEST
        700   | 400    || MED_HIGHEST
        640   | 360    || MED_HIGHEST
        500   | 300    || LOW_HIGHEST
        480   | 270    || LOW_HIGHEST
        400   | 200    || LOW_HIGHEST
    }
}
