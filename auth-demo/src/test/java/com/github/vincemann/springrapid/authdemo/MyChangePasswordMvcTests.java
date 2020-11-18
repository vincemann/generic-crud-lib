package com.github.vincemann.springrapid.authdemo;

import com.github.vincemann.springrapid.authtests.ChangeEmailMvcTests;
import com.github.vincemann.springrapid.authtests.ChangePasswordMvcTests;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.github.vincemann.springrapid.authdemo")
public class MyChangePasswordMvcTests extends ChangePasswordMvcTests {
}