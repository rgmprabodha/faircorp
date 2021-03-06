package com.emse.faircorp.hello;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(OutputCaptureExtension.class)
public class DummyUserServiceTest {

    @Configuration
    @ComponentScan("com.emse.faircorp.hello")
    public static class DummyUserServiceTestConfig {
    }

    @Autowired
    public DummyUserService dummyUserService;

    @Test
    public void testGreetingAll(CapturedOutput output) {
        dummyUserService.greetAll();
        Assertions.assertThat(output.getAll()).contains("Hello, Elodie!", "Hello, Elodie!");
    }
}
