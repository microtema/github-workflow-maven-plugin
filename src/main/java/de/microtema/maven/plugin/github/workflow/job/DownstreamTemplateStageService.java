package de.microtema.maven.plugin.github.workflow.job;

import de.microtema.maven.plugin.github.workflow.PipelineGeneratorMojo;
import de.microtema.maven.plugin.github.workflow.PipelineGeneratorUtil;
import de.microtema.maven.plugin.github.workflow.model.JobData;
import de.microtema.maven.plugin.github.workflow.model.MetaData;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DownstreamTemplateStageService implements TemplateStageService {

    private final List<TemplateStageService> multipleStageTemplateStageServices = new ArrayList<>();
    private final List<TemplateStageService> singleStageTemplateStageServices = new ArrayList<>();

    private final DeploymentTemplateStageService deploymentTemplateStageService;

    public DownstreamTemplateStageService(
            BuildTemplateStageService buildTestTemplateStageService,
            DeploymentTemplateStageService deploymentTemplateStageService,
            PublishTemplateStageService publishTemplateStageService,
            SystemTestTemplateStageService systemTestTemplateStageService,
            PerformanceTestTemplateStageService performanceTestTemplateStageService) {

        multipleStageTemplateStageServices.add(systemTestTemplateStageService);
        multipleStageTemplateStageServices.add(performanceTestTemplateStageService);

        this.deploymentTemplateStageService = deploymentTemplateStageService;

        singleStageTemplateStageServices.add(publishTemplateStageService);
        singleStageTemplateStageServices.add(buildTestTemplateStageService);
    }

    @Override
    public String getJobName() {
        return "downstream";
    }

    @Override
    public boolean access(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (Objects.isNull(mojo.getDownStreams())) {
            return false;
        }

        List<String> stageNames = metaData.getStageNames();

        return mojo.getDownStreams().entrySet().stream().anyMatch(it -> stageNames.contains(it.getKey()));
    }

    @Override
    public String getTemplate(PipelineGeneratorMojo mojo, MetaData metaData) {

        if (!access(mojo, metaData)) {
            return null;
        }

        List<String> stageNames = metaData.getStageNames();

        Map<String, String> downStreams = mojo.getDownStreams();

        boolean multipleStages = stageNames.size() > 1;

        return stageNames.stream().filter(downStreams::containsKey).map(it -> {

            String defaultTemplate = PipelineGeneratorUtil.getTemplate(getTemplateName());

            defaultTemplate = PipelineGeneratorUtil.applyProperties(defaultTemplate, it);

            String needs = getJobNames(mojo, metaData, it);

            String downStream = downStreams.get(it);

            String[] parts = downStream.split(":");

            if (parts.length > 1) {
                downStream = parts[1];
            }

            JobData jobData = getJobData(parts, it);

            String jobId = jobData.getId();
            String jobName = jobData.getName();

            return defaultTemplate
                    .replace("downstream:", multipleStages ? "downstream-" + jobId.toLowerCase() + ":" : "downstream:")
                    .replace("%JOB_NAME%", multipleStages ? jobName + " [" + it.toUpperCase() + "]" : jobName)
                    .replace("%DOWNSTREAM%", downStream)
                    .replace("%NEEDS%", needs);

        }).collect(Collectors.joining("\n"));
    }

    private String getJobNames(PipelineGeneratorMojo mojo, MetaData metaData, String stageName) {

        boolean microserviceRepo = PipelineGeneratorUtil.isMicroserviceRepo(mojo.getProject());

        String needs;

        if (microserviceRepo) {

            needs = multipleStageTemplateStageServices.stream()
                    .filter(t -> t.access(mojo, metaData))
                    .map(t -> t.getJobIds(metaData, stageName))
                    .collect(Collectors.joining(", "));

            if (StringUtils.isEmpty(needs)) {
                needs = deploymentTemplateStageService.getJobIds(metaData, stageName);
            }

            return needs;
        }

        return singleStageTemplateStageServices.stream()
                .filter(t -> t.access(mojo, metaData))
                .map(TemplateStageService::getJobName)
                .findFirst().orElseThrow(() -> new IllegalStateException("Unable to get job name for stage: " + stageName));
    }

    private JobData getJobData(String[] parts, String stageName) {

        String jobId = stageName;
        String jobName = "Downstream";

        if (parts.length > 1) {
            jobId += "-" + parts[0].replaceAll("[^a-zA-Z0-9]", "-");
            jobName = parts[0];
        }

        JobData jobData = new JobData();

        jobData.setId(jobId);
        jobData.setName(jobName);

        return jobData;
    }
}