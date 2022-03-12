package pl.agh.iet.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.scarecraw22.utils.file.FileUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

public class MvcRestRequestBuilder implements MvcRequestBuilder {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    private final Map<String, String> headers = new HashMap<>();

    private boolean withRequestBuilder = false;
    private MockHttpServletRequestBuilder requestBuilder;

    private String url;
    private HttpMethod httpMethod;
    private MediaType contentType;
    private String body;

    public MvcRestRequestBuilder(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Override
    public MvcRequestBuilder withMethod(HttpMethod method) {
        this.httpMethod = method;
        return this;
    }

    @Override
    public MvcRequestBuilder post() {
        this.httpMethod = HttpMethod.POST;
        return this;
    }

    @Override
    public MvcRequestBuilder get() {
        this.httpMethod = HttpMethod.GET;
        return this;
    }

    @Override
    public MvcRequestBuilder delete() {
        this.httpMethod = HttpMethod.DELETE;
        return this;
    }

    @Override
    public MvcRequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public MvcRequestBuilder contentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public MvcRequestBuilder withHeaders(Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers);
        return this;
    }

    @Override
    public MvcRequestBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public MvcRequestBuilder withBodyFromFile(String pathToFile) {
        this.body = FileUtils.readFile(pathToFile);
        return this;
    }

    @Override
    public MvcRequestBuilder withRequestBuilder(MockHttpServletRequestBuilder requestBuilder) {
        this.withRequestBuilder = true;
        this.requestBuilder = requestBuilder;
        return this;
    }

    @Override
    public MvcResponseChecker execute() throws Exception {
        if (withRequestBuilder) {
            return new MvcRestResponseChecker(mockMvc.perform(requestBuilder), objectMapper);
        } else {
            if (httpMethod == null) {
                throw new IllegalStateException("HttpMethod is not set. Use method() method on: " + MvcRestRequestBuilder.class.getName() + " class");
            }
            if (url == null || url.isBlank()) {
                throw new IllegalStateException("Url is not set. Use url method on: " + MvcRestRequestBuilder.class.getName() + " class");
            }
            if (contentType == null) {
                contentType = MediaType.APPLICATION_JSON;
            }
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(httpMethod, url)
                    .contentType(contentType);

            requestBuilder = addHeaders(requestBuilder, headers);
            if (body != null) {
                requestBuilder = requestBuilder.content(body);
            }

            return new MvcRestResponseChecker(mockMvc.perform(requestBuilder), objectMapper);
        }
    }

    private MockHttpServletRequestBuilder addHeaders(MockHttpServletRequestBuilder builder,
                                                     Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder = builder.header(entry.getKey(), entry.getValue());
            }
        }
        return builder;
    }
}
