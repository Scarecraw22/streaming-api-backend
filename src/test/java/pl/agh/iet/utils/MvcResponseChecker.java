package pl.agh.iet.utils;

import org.springframework.http.HttpStatus;

public interface MvcResponseChecker {

    MvcResponseChecker expectStatus(int statusCode) throws Exception;

    MvcResponseChecker expectStatus(HttpStatus httpStatus) throws Exception;

    MvcResponseChecker expectOk() throws Exception;

    MvcResponseChecker expectBadRequest() throws Exception;

    MvcResponseChecker expectNotFound() throws Exception;

    MvcResponseChecker expectConflict() throws Exception;

    MvcResponseChecker expectJson(String body) throws Exception;

    MvcResponseChecker expectJsonFromFile(String pathToFile) throws Exception;

    MvcResponseChecker expectBodyFile(String pathToFile) throws Exception;

    MvcResponseChecker expectBody(String body) throws Exception;

    String getResponseBodyAsString() throws Exception;

    <T> T getResponseBodyAs(Class<T> clazz) throws Exception;
}
