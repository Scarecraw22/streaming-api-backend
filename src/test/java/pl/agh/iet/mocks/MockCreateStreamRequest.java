package pl.agh.iet.mocks;

import lombok.Builder;
import lombok.Value;

import java.io.File;

@Value
@Builder
public class MockCreateStreamRequest {

    String name;
    String title;
    String description;
    File video;
    File thumbnail;
}
