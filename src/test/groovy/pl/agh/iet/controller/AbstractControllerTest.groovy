package pl.agh.iet.controller

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.agh.iet.ffmpeg.FfmpegProperties
import pl.agh.iet.initializers.MongoDbTestInitializer
import pl.agh.iet.utils.MvcRequestBuilder
import pl.agh.iet.utils.MvcRestRequestBuilder
import spock.lang.Specification

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

    MvcRequestBuilder post() {
        return new MvcRestRequestBuilder(mvc, objectMapper)
                .post()
    }

    MvcRequestBuilder get() {
        return new MvcRestRequestBuilder(mvc, objectMapper)
                .get()
    }
}
