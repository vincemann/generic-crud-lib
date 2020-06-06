package io.github.vincemann.springrapid.coretest.service.result.matcher;

/**
 * Matcher that matches a specific condition that shall be met, after a service test is executed with {@link io.github.vincemann.springrapid.coretest.service.ServiceTestTemplate}.
 */
public interface ServiceResultMatcher {
    public void match();
}
