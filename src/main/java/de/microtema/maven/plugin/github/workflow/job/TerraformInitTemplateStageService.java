package de.microtema.maven.plugin.github.workflow.job;

import de.microtema.maven.plugin.github.workflow.PipelineGeneratorMojo;
import de.microtema.maven.plugin.github.workflow.PipelineGeneratorUtil;
import de.microtema.maven.plugin.github.workflow.model.MetaData;

public class TerraformInitTemplateStageService implements TemplateStageService {

    @Override
    public String getTemplateName() {
        return "terraform-init";
    }

    @Override
    public String getJobId() {
        return "initialize";
    }

    @Override
    public boolean access(PipelineGeneratorMojo mojo, MetaData metaData) {

        return PipelineGeneratorUtil.isDeploymentRepo(mojo.getProject());
    }

    @Override
    public String getTemplate(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (!access(mojo, metaData)) {
            return null;
        }

        String defaultTemplate = PipelineGeneratorUtil.getTemplate(getTemplateName());

        String template = PipelineGeneratorUtil.applyProperties(defaultTemplate, metaData.getStageName());

        return template.replace("%WORKING_DIRECTORY%", "./terraform");
    }
}
