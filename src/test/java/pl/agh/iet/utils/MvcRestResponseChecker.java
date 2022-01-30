package pl.agh.iet.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.scarecraw22.utils.file.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class MvcRestResponseChecker implements MvcResponseChecker {

    private final ResultActions resultActions;
    private final ObjectMapper objectMapper;

    public MvcRestResponseChecker(ResultActions resultActions, ObjectMapper objectMapper) {
        this.resultActions = resultActions;
        this.objectMapper = objectMapper;
    }

    @Override
    public MvcResponseChecker expectStatus(int statusCode) throws Exception {
        this.resultActions.andExpect(MockMvcResultMatchers.status().is(statusCode));
        return this;
    }

    @Override
    public MvcResponseChecker expectStatus(HttpStatus httpStatus) throws Exception {
        return expectStatus(httpStatus.value());
    }

    @Override
    public MvcResponseChecker expectOk() throws Exception {
        return expectStatus(HttpStatus.OK);
    }

    @Override
    public MvcResponseChecker expectBadRequest() throws Exception {
        return expectStatus(HttpStatus.BAD_REQUEST);
    }

    @Override
    public MvcResponseChecker expectNotFound() throws Exception {
        return expectStatus(HttpStatus.NOT_FOUND);
    }

    @Override
    public MvcResponseChecker expectConflict() throws Exception {
        return expectStatus(HttpStatus.CONFLICT);
    }

    @Override
    public MvcResponseChecker expectJson(String body) throws Exception {
        this.resultActions.andExpect(MockMvcResultMatchers.content().json(body));
        return this;
    }

    @Override
    public MvcResponseChecker expectJsonFromFile(String pathToFile) throws Exception {
        return expectJson(FileUtils.readFile(pathToFile));
    }

    @Override
    public String getResponseBodyAsString() throws Exception {
        return this.resultActions.andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Override
    public <T> T getResponseBodyAs(Class<T> clazz) throws Exception {
        return objectMapper.readValue(getResponseBodyAsString(), clazz);
    }

    @Override
    public MvcResponseChecker expectBodyFile(String pathToFile) throws Exception {
        return expectBody(FileUtils.readFile(pathToFile));
    }

    @Override
    public MvcResponseChecker expectBody(String body) throws Exception {
        this.resultActions.andExpect(MockMvcResultMatchers.content().string(body.trim()));
        return this;
    }
}
