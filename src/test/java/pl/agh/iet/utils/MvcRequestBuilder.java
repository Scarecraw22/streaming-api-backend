package pl.agh.iet.utils;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

public interface MvcRequestBuilder {

    MvcRequestBuilder withMethod(HttpMethod method);

    MvcRequestBuilder post();

    MvcRequestBuilder get();

    MvcRequestBuilder delete();

    MvcRequestBuilder url(String url);

    MvcRequestBuilder contentType(MediaType contentType);

    MvcRequestBuilder withHeaders(Map<String, String> headers);

    MvcRequestBuilder withBody(String body);

    MvcRequestBuilder withBodyFromFile(String pathToResource);

    MvcRequestBuilder withRequestBuilder(MockHttpServletRequestBuilder requestBuilder);

    MvcResponseChecker execute() throws Exception;

}
