package pl.agh.iet.controller

import org.springframework.http.HttpHeaders
import pl.agh.iet.mocks.MockCreateStreamRequest
import pl.agh.iet.model.CreateStreamResponse
import pl.agh.iet.model.GetVideoDetailsListResponse
import pl.agh.iet.utils.FileUtils
import pl.agh.iet.utils.StringConsts

import java.nio.file.Paths

class StreamingFlowIT extends AbstractControllerTest {

    def "Create then get master file get list then get thumbnail then delete stream"() {
        given:
        MockCreateStreamRequest createStreamRequest = MockCreateStreamRequest.builder()
                .name("test")
                .title("Sample title")
                .description("Sample description")
                .video(FileUtils.getFileFromResources("movie.mp4"))
                .thumbnail(FileUtils.getFileFromResources("thumbnail.jpg"))
                .build()

        when: "Stream is created"
        CreateStreamResponse response = withRequestBuilder(buildStreamRequest(createStreamRequest))
                .execute()
                .expectOk()
                .getResponseBodyAs(CreateStreamResponse.class)
        def streamId = response.getId()

        then: "Stream created with success and id is returned"
        noExceptionThrown()
        assertStreamCreatedWithSuccess(streamId, createStreamRequest)

        when: "Master file is properly retrieved"
        get()
                .url("/streaming-api/test/master.m3u8")
                .withHeaders(Map.of(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .execute()
                .expectOk()

        then:
        noExceptionThrown()

        when: "All videos are requested"
        GetVideoDetailsListResponse videoDetailsResponse = get()
                .url("/video-details")
                .withHeaders(Map.of(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .execute()
                .expectOk()
                .getResponseBodyAs(GetVideoDetailsListResponse.class)

        then: "List of videos is returned"
        videoDetailsResponse.getDetailsList().size() == 1
        def videoDetails = videoDetailsResponse.getDetailsList().get(0)
        videoDetails.getId() == streamId
        videoDetails.getStreamName() == "test"
        videoDetails.getTitle() == "Sample title"
        videoDetails.getDescription() == "Sample description"
        videoDetails.getCreatedAt() != null
        videoDetails.getMasterLink() == "http://localhost:8080/streaming-api/test/master.m3u8"
        videoDetails.getThumbnailLink() == "http://localhost:8080/thumbnail/test"

        when: "Thumbnail is retrieved"
        get()
                .url("/thumbnail/test")
                .withHeaders(Map.of(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .execute()
                .expectOk()

        then:
        noExceptionThrown()

        when: "Stream is deleted"
        delete()
                .url("/streaming-api")
                .withHeaders(Map.of(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .withBody("""
{
    "id": "$streamId"
}
""")
                .execute()
                .expectOk()

        then: "All stream data is deleted"
        def thumbnail = Paths.get(thumbnailProperties.getPath())
                .resolve(createStreamRequest.getName())
                .resolve(createStreamRequest.getName() + StringConsts.UNDERSCORE + createStreamRequest.getThumbnail().getName())
                .toFile()
        def videoChunksDir = Paths.get(ffmpegProperties.getOutputDir()).resolve("test").toFile()
        assert !metadataRepository.existsById(streamId)
        assert !videoChunksDir.exists()
        assert !thumbnail.exists()

        cleanup:
        metadataRepository.deleteAll()
        // In case of an exception
        thumbnail.delete()
        videoChunksDir.delete()
    }
}
