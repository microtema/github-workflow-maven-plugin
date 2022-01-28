package de.microtema.maven.plugin.github.workflow.job;

import de.microtema.maven.plugin.github.workflow.PipelineGeneratorMojo;
import de.microtema.maven.plugin.github.workflow.PipelineGeneratorUtil;
import de.microtema.maven.plugin.github.workflow.model.MetaData;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public class PackageTemplateStageService implements TemplateStageService {

    @Override
    public boolean access(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (!PipelineGeneratorUtil.existsDockerfile(mojo.getProject())) {
            return false;
        }

        return Stream.of("feature", "bugfix").noneMatch(it -> StringUtils.equalsIgnoreCase(metaData.getBranchName(), it));
    }

    @Override
    public String getTemplate(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (!access(mojo, metaData)) {
            return null;
        }

        String template = PipelineGeneratorUtil.getTemplate(getName());

        if (!StringUtils.equalsIgnoreCase(metaData.getBranchName(), "master")) {

            template = template.replace("$VERSION.$GITHUB_SHA", "$VERSION");
        }

        return template.replace("%POM_ARTIFACT%", "true");
    }
}
