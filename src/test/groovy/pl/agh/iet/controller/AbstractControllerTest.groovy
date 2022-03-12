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
import pl.agh.iet.db.repository.MetadataRepository
import pl.agh.iet.ffmpeg.FfmpegProperties
import pl.agh.iet.initializers.MongoDbTestInitializer
import pl.agh.iet.mocks.MockCreateStreamRequest
import pl.agh.iet.service.thumbnail.ThumbnailProperties
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
abstract class AbstractControllerTest extends Specification {

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
}
