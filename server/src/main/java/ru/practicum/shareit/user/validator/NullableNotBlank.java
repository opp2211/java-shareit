package ru.practicum.shareit.user.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NullableNotBlankValidator.class)
public @interface NullableNotBlank {
    String message() default "{javax.validation.constraints.NullableNotBlank.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}