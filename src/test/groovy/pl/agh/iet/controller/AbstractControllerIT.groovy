package pl.agh.iet.controller

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.agh.iet.db.MetadataEntity
import pl.agh.iet.db.repository.MetadataRepository
import pl.agh.iet.ffmpeg.FfmpegProperties
import pl.agh.iet.initializers.MongoDbTestInitializer
import pl.agh.iet.mocks.MockCreateStreamRequest
import pl.agh.iet.model.CreateStreamResponse
import pl.agh.iet.model.GetVideoDetailsListResponse
import pl.agh.iet.service.thumbnail.ThumbnailProperties
import pl.agh.iet.utils.FileUtils
import pl.agh.iet.utils.MvcRequestBuilder
import pl.agh.iet.utils.MvcRestRequestBuilder
import pl.agh.iet.utils.StringConsts
import spock.lang.Specification

import java.nio.file.Paths

@Slf4j
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(
        initializers = [MongoDbTestInitializer.class]
)
abstract class AbstractControllerIT extends Specification {

    @Autowired
    protected MockMvc mvc

    @Autowired
    protected ObjectMapper objectMapper

    @Autowired
    protected FfmpegProperties ffmpegProperties

    @Autowired
    protected ThumbnailProperties thumbnailProperties

    @Autowired
    protected MetadataRepository metadataRepository

    MvcRequestBuilder post() {
        return new MvcRestRequestBuilder(mvc, objectMapper)
                .post()
    }

    MvcRequestBuilder get() {
        return new MvcRestRequestBuilder(mvc, objectMapper)
                .get()
    }

    MvcRequestBuilder delete() {
        return new MvcRestRequestBuilder(mvc, objectMapper)
                .delete()
    }

    MvcRequestBuilder withRequestBuilder(MockHttpServletRequestBuilder requestBuilder) {
        return new MvcRestRequestBuilder(mvc, objectMapper)
                .withRequestBuilder(requestBuilder)
    }

    protected MockHttpServletRequestBuilder buildStreamRequest(MockCreateStreamRequest request) {
        byte[] videoBytes = request.getVideo().getBytes()
        MockMultipartFile video = new MockMultipartFile("content", request.getVideo().getName(), MediaType.MULTIPART_FORM_DATA_VALUE, videoBytes)
        byte[] thumbnailBytes = request.getThumbnail().getBytes()
        MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnail", request.getThumbnail().getName(), MediaType.MULTIPART_FORM_DATA_VALUE, thumbnailBytes)

        return MockMvcRequestBuilders.multipart("/streaming-api/video")
                .file(video)
                .file(thumbnailFile)
                .param("name", request.getName())
                .param("description", request.getDescription())
                .param("title", request.getTitle())
                .header(HttpHeaders.ORIGIN, "http://any-origin.pl")
    }

    protected void assertStreamCreatedWithSuccess(String id, MockCreateStreamRequest request) {
        assert id != null
        assert !id.isBlank()
        assert metadataRepository.existsById(id)

        def thumbnailFilename = request.getName() + StringConsts.UNDERSCORE + request.getThumbnail().getName()
        def thumbnail = Paths.get(thumbnailProperties.getPath())
                .resolve(request.getName())
                .resolve(thumbnailFilename)
                .toFile()

        assert thumbnail.exists()

        def metadata = metadataRepository.findById(id).get()
        assert metadata.getThumbnailFilename() == thumbnailFilename
        assert metadata.getTitle() == request.getTitle()
        assert metadata.getStreamName() == request.getName()
        assert metadata.getDescription() == request.getDescription()
        assert metadata.getCreatedAt() != null
    }

    protected MockCreateStreamRequest createStreamRequest(String streamName, String title, String description) {
        return MockCreateStreamRequest.builder()
                .name(streamName)
                .title(title)
                .description(description)
                .video(FileUtils.getFileFromResources("movie.mp4"))
                .thumbnail(FileUtils.getFileFromResources("thumbnail.jpg"))
                .build()
    }

    protected String createStreamAndExpectOkThenReturnId(String streamName, String title, String description) {
        MockCreateStreamRequest createStreamRequest = createStreamRequest(streamName, title, description)

        CreateStreamResponse response = withRequestBuilder(buildStreamRequest(createStreamRequest))
                .execute()
                .expectOk()
                .getResponseBodyAs(CreateStreamResponse.class)

        return response.getId()
    }

    protected String createStreamAndExpectOkThenReturnId(MockCreateStreamRequest createStreamRequest) {

        return createStreamAndExpectOkThenReturnId(createStreamRequest.getName(), createStreamRequest.getTitle(), createStreamRequest.getDescription())
    }

    protected void deleteStreamAndExpectSuccess(String id) {
        MetadataEntity entity = metadataRepository.findById(id).get()

        delete()
                .url("/streaming-api")
                .withHeaders(Map.of(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .withBody("""
{
    "id": "$id"
}
""")
                .execute()
                .expectOk()


        def thumbnail = Paths.get(thumbnailProperties.getPath())
                .resolve(entity.getStreamName())
                .resolve(entity.getThumbnailFilename())
                .toFile()
        def videoChunksDir = Paths.get(ffmpegProperties.getOutputDir()).resolve(entity.getStreamName()).toFile()
        assert !metadataRepository.existsById(id)
        assert !videoChunksDir.exists()
        assert !thumbnail.exists()
    }

    protected void searchWithInvailidRequestAndExpectBadRequest() {
        def requestBuilder = MockMvcRequestBuilders.multipart("/video-details/search")
                .header(HttpHeaders.ORIGIN, "http://any-origin.pl")

        withRequestBuilder(requestBuilder)
                .execute()
                .expectBadRequest()
    }

    protected void searchWithInvailidRequestAndExpectBadRequest(String title) {
        def requestBuilder = MockMvcRequestBuilders.multipart("/video-details/search")
                .param("title", title)
                .header(HttpHeaders.ORIGIN, "http://any-origin.pl")

        withRequestBuilder(requestBuilder)
                .execute()
                .expectBadRequest()
    }

    protected List<GetVideoDetailsListResponse.Video> searchByTitleAndExpectSuccess(String title) {
        def requestBuilder = MockMvcRequestBuilders.multipart("/video-details/search")
                .param("title", title)
                .header(HttpHeaders.ORIGIN, "http://any-origin.pl")

        List<GetVideoDetailsListResponse.Video> detailsList = withRequestBuilder(requestBuilder)
                .execute()
                .expectOk()
                .getResponseBodyAs(GetVideoDetailsListResponse.class)
                .getDetailsList()

        return detailsList;
    }
}
