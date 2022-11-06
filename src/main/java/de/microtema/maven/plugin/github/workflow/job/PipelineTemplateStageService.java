package de.microtema.maven.plugin.github.workflow.job;

import de.microtema.maven.plugin.github.workflow.PipelineGeneratorMojo;
import de.microtema.maven.plugin.github.workflow.PipelineGeneratorUtil;
import de.microtema.maven.plugin.github.workflow.model.MetaData;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public class PipelineTemplateStageService implements TemplateStageService {

    @Override
    public String getTemplate(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (Stream.of("feature", "bugfix").anyMatch(it -> StringUtils.equalsIgnoreCase(metaData.getBranchName(), it))) {
            return PipelineGeneratorUtil.getTemplate(getTemplateName());
        }

        if (!PipelineGeneratorUtil.isDeploymentRepo(mojo.getProject())) {
            return PipelineGeneratorUtil.getTemplate(getTemplateName());
        }

        return PipelineGeneratorUtil.getTemplate("deployment-pipeline");
    }
}
