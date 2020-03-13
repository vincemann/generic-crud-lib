package io.github.vincemann.generic.crud.lib.test.controller;

import io.github.vincemann.generic.crud.lib.config.JacksonConfig;
import io.github.vincemann.generic.crud.lib.test.InitializingTest;
import io.github.vincemann.generic.crud.lib.test.automockBeans.AutoMockServiceBeansGenericAnnotationWebConfigContextLoader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Getter
@Setter
@Slf4j
@ActiveProfiles(value = {"test","web","webTest"})
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(
        loader = AutoMockServiceBeansGenericAnnotationWebConfigContextLoader.class//,
)
//smh he does not find property sources anymore without explicitly specifying with that setup
@PropertySource({"classpath:application.properties","classpath:application-test.properties"})
public abstract class MvcControllerTest extends InitializingTest {
    private MockMvc mockMvc;

    @BeforeEach
    public void setupMvc(WebApplicationContext wac) {
        String mediaType = MediaType.APPLICATION_JSON_UTF8_VALUE;
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultRequest(get("/")
                        .accept(mediaType)
                        .contentType(mediaType)
                )
                .alwaysExpect(content().contentType(mediaType))
                .alwaysDo(print())
                .build();
    }
}
