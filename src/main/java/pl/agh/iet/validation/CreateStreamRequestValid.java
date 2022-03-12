package pl.agh.iet.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.agh.iet.model.CreateStreamRequest;
import pl.agh.iet.service.video.VideoService;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateStreamRequestValid.Validator.class)
@Documented
public @interface CreateStreamRequestValid {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Component
    @RequiredArgsConstructor
    class Validator implements ConstraintValidator<CreateStreamRequestValid, CreateStreamRequest> {

        private final VideoService videoService;

        @Override
        public boolean isValid(CreateStreamRequest request, ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();

            boolean containsWhiteSpace = request.getName().matches(".*\\s.*");
            if (containsWhiteSpace) {
                context.buildConstraintViolationWithTemplate("Name cannot contain any whitespace characters")
                        .addPropertyNode("name")
                        .addConstraintViolation();
                return false;
            }

            if (videoService.streamExists(request.getName())) {
                context.buildConstraintViolationWithTemplate("Stream already exists")
                        .addPropertyNode("name")
                        .addConstraintViolation();
                return false;
            }

            return true;
        }
    }
}
