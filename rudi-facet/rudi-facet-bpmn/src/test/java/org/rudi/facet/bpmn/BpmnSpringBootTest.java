package org.rudi.facet.bpmn;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest(classes = BpmnSpringBootTestApplication.class)
@TestPropertySource(properties = "spring.config.name = facetbpmn")
@ActiveProfiles(profiles = { "test", "${spring.profiles.test:test-env}" })
public @interface BpmnSpringBootTest {
}
