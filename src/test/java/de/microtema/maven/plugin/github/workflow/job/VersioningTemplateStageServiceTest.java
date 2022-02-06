package de.microtema.maven.plugin.github.workflow.job;

import de.microtema.model.builder.util.FieldInjectionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersioningTemplateStageServiceTest {

    @Inject
    VersioningTemplateStageService sut;

    @BeforeEach
    void setUp() {

        FieldInjectionUtil.injectFields(this);
    }

    @Test
    void getName() {

        String answer = sut.getTemplateName();

        assertEquals("versioning", answer);
    }
}
