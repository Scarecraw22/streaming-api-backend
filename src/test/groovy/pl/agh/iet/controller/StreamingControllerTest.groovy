package pl.agh.iet.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.agh.iet.db.repository.MetadataRepository
import pl.agh.iet.model.CreateStreamResponse
import pl.agh.iet.utils.FileUtils

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class StreamingControllerTest extends AbstractControllerTest {

    @Autowired
    private MetadataRepository metadataRepository

    def "create sample video"() {
        given:
        byte[] videoBytes = FileUtils.getFileFromResources("movie.mp4").getBytes()
        MockMultipartFile video = new MockMultipartFile("content", "movie.mp4", MediaType.MULTIPART_FORM_DATA_VALUE, videoBytes)
        byte[] thumbnailBytes = FileUtils.getFileFromResources("thumbnail.jpg").getBytes()
        MockMultipartFile thumbnailFile = new MockMultipartFile("thumbnail", "thumbnail.jpg", MediaType.MULTIPART_FORM_DATA_VALUE, thumbnailBytes)

        def multipartBuilder = MockMvcRequestBuilders.multipart("/streaming-api/video")
                .file(video)
                .file(thumbnailFile)
                .param("name", "test")
                .param("description", "Sample description")

        when:
        String responseBody = mvc.perform(multipartBuilder.header(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8)

        CreateStreamResponse response = objectMapper.readValue(responseBody, CreateStreamResponse.class)

        then:
        noExceptionThrown()
        response.getId() != null
        !response.getId().isBlank()
        metadataRepository.existsById(response.getId())
        def thumbnail = Paths.get(thumbnailProperties.getPath())
                .resolve("test")
                .resolve("test_thumbnail.jpg")
                .toFile()
        thumbnail.exists()

        when:
        get()
                .url("/streaming-api/test/master.m3u8")
                .withHeaders(Map.of(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .execute()
                .expectOk()

        then:
        noExceptionThrown()

        cleanup:
        assert Paths.get(ffmpegProperties.getOutputDir()).resolve("test").deleteDir()
        assert thumbnail.delete()
        metadataRepository.deleteAll()
    }
}
