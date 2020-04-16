package io.github.vincemann.springrapid.coretest.controller.rapid;

import io.github.vincemann.springrapid.core.model.IdentifiableEntity;
import io.github.vincemann.springrapid.core.service.CrudService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.Serializable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Extension of {@link MvcRapidControllerTest} expecting {@link io.github.vincemann.springrapid.core.controller.rapid.idFetchingStrategy.UrlParamIdFetchingStrategy}.
 */
public abstract class UrlParamIdRapidControllerTest<S extends CrudService<E,Id,? extends CrudRepository<E,Id>>
        ,E extends IdentifiableEntity<Id>,
        Id extends Serializable>
        extends MvcRapidControllerTest<S, E, Id> {

    @Value("${controller.idFetchingStrategy.idUrlParamKey:id}")
    private String entityIdParamKey;


    public MockHttpServletRequestBuilder delete(Id id) throws Exception {
        return MockMvcRequestBuilders.delete(getDeleteUrl())
        .param(entityIdParamKey,id.toString());
    }

    public MockHttpServletRequestBuilder find(Id id) throws Exception {
        return get(getFindUrl())
                .param(entityIdParamKey,id.toString());
    }



}
