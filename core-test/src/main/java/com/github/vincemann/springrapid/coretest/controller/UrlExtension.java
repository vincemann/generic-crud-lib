package com.github.vincemann.springrapid.coretest.controller;

import com.github.vincemann.springrapid.core.controller.GenericCrudController;
import com.github.vincemann.springrapid.core.service.filter.ArgAware;
import com.github.vincemann.springrapid.core.service.filter.EntityFilter;
import com.github.vincemann.springrapid.core.service.filter.jpa.EntitySortingStrategy;
import com.github.vincemann.springrapid.core.service.filter.jpa.QueryFilter;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Represents either an {@link EntityFilter}, {@link QueryFilter} or {@link EntitySortingStrategy}.
 * Can be converted to String via {@link com.github.vincemann.springrapid.coretest.util.RapidTestUtil#createExtensionsString(List, ApplicationContext)}
 * which can be added as url param for some endpoints like {@link GenericCrudController#findAll(HttpServletRequest, HttpServletResponse)}.
 *
 * Use {@link com.github.vincemann.springrapid.coretest.controller.template.AbstractCrudControllerTestTemplate#findAll(UrlExtension...)} and similar helper methods
 * to add extensions while testing, whenever possible.
 *
 * If not possible call {@link com.github.vincemann.springrapid.coretest.util.RapidTestUtil#addUrlExtensionsToRequest(ApplicationContext, MockHttpServletRequestBuilder, UrlExtension...)} to add
 * to mvc request.
 */
@Getter
public class UrlExtension {

    Class<? extends ArgAware> extensionType;
    String[] args;

    public UrlExtension(Class<? extends ArgAware> extensionType, String... args) {
        this.extensionType = extensionType;
        this.args = args;
    }

//    public String getUrlParamKey(){
//        if (QueryFilter.class.isAssignableFrom(this.getExtensionType())){
//            return GenericCrudController.QUERY_FILTER_URL_KEY;
//        }
//        else if (EntityFilter.class.isAssignableFrom(this.getExtensionType())){
//            return GenericCrudController.ENTITY_FILTER_URL_KEY;
//        }else if (EntitySortingStrategy.class.isAssignableFrom(this.getExtensionType())){
//            return GenericCrudController.ENTITY_SORTING_STRATEGY_URL_KEY;
//        }else{
//            throw new IllegalArgumentException("unknown extension type: " + this.getExtensionType());
//        }
//    }

}
