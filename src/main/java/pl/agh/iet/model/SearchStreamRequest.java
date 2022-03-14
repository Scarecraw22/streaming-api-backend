package pl.agh.iet.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Value
@Builder
@Jacksonized
public class SearchStreamRequest {

    @NotBlank(message = "title cannot be blank")
    String title;
}
