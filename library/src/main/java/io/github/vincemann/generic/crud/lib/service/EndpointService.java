package io.github.vincemann.generic.crud.lib.service;

import io.github.vincemann.generic.crud.lib.advice.log.LogInteraction;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
public class EndpointService {


    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public EndpointService(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @LogInteraction
    public void addMapping(RequestMappingInfo requestMappingInfo, Method requestMethod, Object controller)  {

        /*RequestMappingInfo requestMappingInfo = RequestMappingInfo
                .paths(urlPath)
                .methods(RequestMethod.GET)
                .produces(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .build();*/

        requestMappingHandlerMapping.
                registerMapping(requestMappingInfo, controller,
                        requestMethod
                );
    }

}