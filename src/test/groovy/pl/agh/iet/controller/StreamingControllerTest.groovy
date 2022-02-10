package pl.agh.iet.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.agh.iet.utils.FileUtils

import java.nio.file.Paths

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class StreamingControllerTest extends AbstractControllerTest {

    def "create sample video"() {
        given:
        byte[] videoBytes = FileUtils.getFileFromResources("movie.mp4").getBytes()
        MockMultipartFile video = new MockMultipartFile("content", "movie.mp4", MediaType.MULTIPART_FORM_DATA_VALUE, videoBytes)
        byte[] thumbnailBytes = FileUtils.getFileFromResources("thumbnail.jpg").getBytes()
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.jpg", MediaType.MULTIPART_FORM_DATA_VALUE, thumbnailBytes)

        def multipartBuilder = MockMvcRequestBuilders.multipart("/streaming-api/video")
                .file(video)
                .file(thumbnail)
                .param("name", "test")
                .param("description", "Sample description")

        when:
        mvc.perform(multipartBuilder.header(HttpHeaders.ORIGIN, "http://any-origin.pl"))
                .andExpect(status().isOk())

        then:
        noExceptionThrown()

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
    }
}
