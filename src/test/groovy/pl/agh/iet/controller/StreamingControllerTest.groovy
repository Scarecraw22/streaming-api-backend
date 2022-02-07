package pl.agh.iet.controller

import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.agh.iet.utils.FileUtils

import java.nio.file.Paths

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class StreamingControllerTest extends AbstractControllerTest {

    def "create sample video"() {
        given:
        byte[] bytes = FileUtils.getFileFromResources("big_jack.mp4").getBytes()
        MockMultipartFile file = new MockMultipartFile("content", "big_jack.mp4", MediaType.MULTIPART_FORM_DATA_VALUE, bytes)
        def multipartBuilder = MockMvcRequestBuilders.multipart("/streaming-api/video")
                .file(file)
                .param("name", "test")

        when:
        mvc.perform(multipartBuilder)
                .andExpect(status().isOk())

        then:
        noExceptionThrown()

        when:
        get()
                .url("/streaming-api/test")
                .execute()
                .expectOk()

        then:
        noExceptionThrown()

        cleanup:
        assert Paths.get(ffmpegProperties.getOutputDir()).resolve("test").deleteDir()
    }
}
