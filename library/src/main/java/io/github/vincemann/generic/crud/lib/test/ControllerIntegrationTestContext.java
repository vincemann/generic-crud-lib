package io.github.vincemann.generic.crud.lib.test;

import io.github.vincemann.generic.crud.lib.controller.dtoMapper.DtoMappingContext;
import io.github.vincemann.generic.crud.lib.controller.springAdapter.DtoCrudController_SpringAdapter;
import io.github.vincemann.generic.crud.lib.controller.springAdapter.idFetchingStrategy.UrlParamIdFetchingStrategy;
import io.github.vincemann.generic.crud.lib.model.IdentifiableEntity;
import io.github.vincemann.generic.crud.lib.service.CrudService;
import io.github.vincemann.generic.crud.lib.test.controller.springAdapter.BaseAddressProvider;
import io.github.vincemann.generic.crud.lib.test.equalChecker.EqualChecker;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

@Getter
@Setter
public abstract class ControllerIntegrationTestContext<E extends IdentifiableEntity<Id>, Id extends Serializable>
            implements BaseAddressProvider, InitializingBean {

    private static final String LOCAL_HOST = "http://127.0.0.1";
    private static TestRestTemplate restTemplate;
    @LocalServerPort
    private String port;
    private Class<E> entityClass = (Class<E>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    //todo muss raus hier, wir hardcoden nicht auf url param id strategy

    private DtoMappingContext<Id> dtoMappingContext;
    private String url;
    private CrudService<E,Id, CrudRepository<E,Id>> testService;
    private DtoCrudController_SpringAdapter<E, Id, CrudRepository<E,Id>> controller;
    private EqualChecker<? extends IdentifiableEntity<Id>> defaultDtoEqualChecker;

    public ControllerIntegrationTestContext(String url) {
        this.url = url;
    }

    public ControllerIntegrationTestContext() {
        this.url= LOCAL_HOST;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.entityIdParamKey = ((UrlParamIdFetchingStrategy<Id>) getController().getIdIdFetchingStrategy()).getIdUrlParamKey();
        //user might want to inject own beans that are diff from controllers beans -> null checks
        if(dtoMappingContext ==null) {
            dtoMappingContext = getController().getDtoMappingContext();
        }
        if (testService == null) {
            setTestService(getController().getCrudService());
        }
    }

    @BeforeAll
    public static void setUp() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().build());
        clientHttpRequestFactory.setBufferRequestBody(false);
        restTemplate = new TestRestTemplate();
        restTemplate.getRestTemplate().setRequestFactory(clientHttpRequestFactory);
    }

    public String getUrlWithPort(){
        return url+":"+port;
    }

    @Override
    public String provideAddress() {
        return getUrlWithPort();
    }

    public TestRestTemplate getRestTemplate() {
        return restTemplate;
    }
}