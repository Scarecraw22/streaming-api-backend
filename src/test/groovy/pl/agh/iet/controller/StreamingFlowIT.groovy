package pl.agh.iet.controller

import org.springframework.http.HttpHeaders
import pl.agh.iet.mocks.MockCreateStreamRequest
import pl.agh.iet.model.GetVideoDetailsListResponse

class StreamingFlowIT extends AbstractControllerIT {

    def setup() {
        deleteAll()
    }

    def "Create then get master file get list then get thumbnail then delete stream"() {
        given:
        MockCreateStreamRequest createStreamRequest = createStreamRequest("test", "Sample title", "Sample description")

        when: "Stream is created"
        def streamId = createStreamAndExpectOkThenReturnId(createStreamRequest)

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
        deleteStreamAndExpectSuccess(streamId)

        then: "All stream data is deleted"
        noExceptionThrown()

        cleanup:
        metadataRepository.deleteAll()
    }
}
