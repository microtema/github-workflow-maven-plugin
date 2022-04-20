package de.microtema.maven.plugin.github.workflow.job;

import de.microtema.maven.plugin.github.workflow.PipelineGeneratorMojo;
import de.microtema.maven.plugin.github.workflow.PipelineGeneratorUtil;
import de.microtema.maven.plugin.github.workflow.model.MetaData;

public class FlywayTemplateStageService implements TemplateStageService {

    @Override
    public String getJobId() {
        return "db-migration";
    }

    @Override
    public boolean access(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (!metaData.isDeployable()) {
            return false;
        }

        return PipelineGeneratorUtil.existsFlyway(mojo.getProject());
    }

    @Override
    public String getTemplate(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (!access(mojo, metaData)) {
            return null;
        }

        return PipelineGeneratorUtil.getTemplate(getTemplateName());
    }
}
