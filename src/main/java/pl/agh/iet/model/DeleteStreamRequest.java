package pl.agh.iet.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Value
@Builder
@Jacksonized
public class DeleteStreamRequest {

    @NotBlank(message = "id cannot be blank")
    String id;
}
