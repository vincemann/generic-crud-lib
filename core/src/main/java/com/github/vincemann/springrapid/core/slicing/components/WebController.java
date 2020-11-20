package com.github.vincemann.springrapid.core.slicing.components;

import com.github.vincemann.springrapid.core.RapidProfiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Inherited
@Profile(RapidProfiles.WEB)
@Controller
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebController {
}
