package de.microtema.maven.plugin.github.workflow;

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineGeneratorMojoMavenLibraryTest {

    @InjectMocks
    PipelineGeneratorMojo sut;

    @Mock
    MavenProject project;

    @Mock
    File basePath;

    @Mock
    Properties properties;

    File pipelineFile;

    @BeforeEach
    void setUp() {

        sut.project = project;

        sut.githubWorkflowsDir = "./target/.github/workflows";

        sut.runsOn = "self-hosted,azure-runners";
    }

    @Test
    void generateDevelopWorkflowFile() throws Exception {

        when(project.getBasedir()).thenReturn(basePath);
        when(basePath.getPath()).thenReturn(".");
        when(project.getName()).thenReturn("github-workflows-maven-plugin Maven Mojo");
        when(project.getProperties()).thenReturn(properties);
        Map<Object, Object> stringStringMap = Collections.singletonMap("sonar.url", "http://localhost:9000");
        when(properties.entrySet()).thenReturn(stringStringMap.entrySet());

        sut.stages.put("local", "feature/*");

        pipelineFile = new File(sut.githubWorkflowsDir, "feature-workflow.yaml");

        sut.execute();

        String answer = FileUtils.readFileToString(pipelineFile, "UTF-8");

        assertEquals("name: github-workflows-maven-plugin Maven Mojo [local]\n" +
                "\n" +
                "on:\n" +
                "  push:\n" +
                "    branches:\n" +
                "      - feature/*\n" +
                "  pull_request:\n" +
                "    branches:\n" +
                "      - feature/*\n" +
                "\n" +
                "env:\n" +
                "  GITHUB_TOKEN: \"${{ secrets.GITHUB_TOKEN }}\"\n" +
                "  SONAR_TOKEN: \"${{ secrets.SONAR_TOKEN }}\"\n" +
                "  JAVA_VERSION: \"17.x\"\n" +
                "  MAVEN_CLI_OPTS: \"-s settings.xml --batch-mode --errors --fail-at-end --show-version\\\n" +
                "    \\ -DinstallAtEnd=true -DdeployAtEnd=true\"\n" +
                "\n" +
                "jobs:\n" +
                "  compile:\n" +
                "    name: Compile\n" +
                "    runs-on: [ self-hosted, azure-runners ]\n" +
                "    needs: [ ]\n" +
                "    steps:\n" +
                "      - name: 'Checkout'\n" +
                "        uses: actions/checkout@v2\n" +
                "      - name: 'Java: Setup'\n" +
                "        uses: actions/setup-java@v1\n" +
                "        with:\n" +
                "          java-version: ${{ env.JAVA_VERSION }}\n" +
                "      - name: 'Artifact: download'\n" +
                "        if: false\n" +
                "        uses: actions/download-artifact@v2\n" +
                "        with:\n" +
                "          name: pom-artifact\n" +
                "      - name: 'Maven: compile'\n" +
                "        run: mvn compile $MAVEN_CLI_OPTS\n" +
                "\n" +
                "  security_check:\n" +
                "    name: Security Check\n" +
                "    runs-on: [ self-hosted, azure-runners ]\n" +
                "    needs: [ compile ]\n" +
                "    steps:\n" +
                "      - name: 'Checkout'\n" +
                "        uses: actions/checkout@v2\n" +
                "      - name: 'Java: Setup'\n" +
                "        uses: actions/setup-java@v1\n" +
                "        with:\n" +
                "          java-version: ${{ env.JAVA_VERSION }}\n" +
                "      - name: 'Maven: dependency-check'\n" +
                "        run: mvn dependency-check:help -P security -Ddownloader.quick.query.timestamp=false $MAVEN_CLI_OPTS\n" +
                "\n" +
                "  unit-test:\n" +
                "    name: Unit Test\n" +
                "    runs-on: [ self-hosted, azure-runners ]\n" +
                "    needs: [ compile ]\n" +
                "    steps:\n" +
                "      - name: 'Checkout'\n" +
                "        uses: actions/checkout@v2\n" +
                "      - name: 'Java: Setup'\n" +
                "        uses: actions/setup-java@v1\n" +
                "        with:\n" +
                "          java-version: ${{ env.JAVA_VERSION }}\n" +
                "      - name: 'Artifact: download'\n" +
                "        if: false\n" +
                "        uses: actions/download-artifact@v2\n" +
                "        with:\n" +
                "          name: pom-artifact\n" +
                "      - name: 'Maven: test'\n" +
                "        run: mvn test $MAVEN_CLI_OPTS\n" +
                "      - name: 'Artifact: prepare'\n" +
                "        run: |\n" +
                "          mkdir -p artifact\n" +
                "          mv target artifact/target\n" +
                "      - name: 'Test result'\n" +
                "        uses: actions/upload-artifact@v2\n" +
                "        with:\n" +
                "          name: target-artifact\n" +
                "          path: artifact/target\n" +
                "\n" +
                "  it-test:\n" +
                "    name: Integration Test\n" +
                "    runs-on: [ self-hosted, azure-runners ]\n" +
                "    needs: [ compile ]\n" +
                "    steps:\n" +
                "      - name: 'Checkout'\n" +
                "        uses: actions/checkout@v2\n" +
                "      - name: 'Java: Setup'\n" +
                "        uses: actions/setup-java@v1\n" +
                "        with:\n" +
                "          java-version: ${{ env.JAVA_VERSION }}\n" +
                "      - name: 'Artifact: download'\n" +
                "        if: false\n" +
                "        uses: actions/download-artifact@v2\n" +
                "        with:\n" +
                "          name: pom-artifact\n" +
                "      - name: 'Maven: integration-test'\n" +
                "        run: mvn integration-test -Dsurefire.skip=true $MAVEN_CLI_OPTS\n" +
                "\n" +
                "  quality-gate:\n" +
                "    name: Quality Gate\n" +
                "    runs-on: [ self-hosted, azure-runners ]\n" +
                "    needs: [ unit-test, it-test ]\n" +
                "    steps:\n" +
                "      - name: 'Checkout'\n" +
                "        uses: actions/checkout@v2\n" +
                "      - name: 'Java: Setup'\n" +
                "        uses: actions/setup-java@v1\n" +
                "        with:\n" +
                "          java-version: ${{ env.JAVA_VERSION }}\n" +
                "      - name: 'Artifact: download'\n" +
                "        uses: actions/download-artifact@v2\n" +
                "        if: false\n" +
                "        with:\n" +
                "          name: target-artifact\n" +
                "      - name: 'Maven: sonar'\n" +
                "        run: |\n" +
                "          mvn verify -DskipTests=true -DskipITs=true -DskipUTs=true $MAVEN_CLI_OPTS\n" +
                "          mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN $MAVEN_CLI_OPTS\n" +
                "\n" +
                "  build:\n" +
                "    name: Build\n" +
                "    runs-on: [ self-hosted, azure-runners ]\n" +
                "    needs: [ quality-gate ]\n" +
                "    steps:\n" +
                "      - name: 'Checkout'\n" +
                "        uses: actions/checkout@v2\n" +
                "      - name: 'Java: Setup'\n" +
                "        uses: actions/setup-java@v1\n" +
                "        with:\n" +
                "          java-version: ${{ env.JAVA_VERSION }}\n" +
                "      - name: 'Artifact: download'\n" +
                "        if: false\n" +
                "        uses: actions/download-artifact@v2\n" +
                "        with:\n" +
                "          name: pom-artifact\n" +
                "      - name: 'Maven: package'\n" +
                "        run: mvn package -P prod -Dcode.coverage=0.0 -DskipTests=true $MAVEN_CLI_OPTS\n" +
                "      - name: 'Artifact: prepare'\n" +
                "        run: |\n" +
                "          mkdir -p artifact/target\n" +
                "          mv target artifact/target\n" +
                "      - name: 'Artifact: upload'\n" +
                "        uses: actions/upload-artifact@v2\n" +
                "        with:\n" +
                "          name: target-artifact\n" +
                "          path: artifact/target\n" +
                "\n" +
                "  publish:\n" +
                "    name: Publish\n" +
                "    runs-on: [ self-hosted, azure-runners ]\n" +
                "    needs: [ build ]\n" +
                "    steps:\n" +
                "      - name: 'Checkout'\n" +
                "        uses: actions/checkout@v2\n" +
                "      - name: 'Java: Setup'\n" +
                "        uses: actions/setup-java@v1\n" +
                "        with:\n" +
                "          java-version: ${{ env.JAVA_VERSION }}\n" +
                "      - name: 'Maven: deploy'\n" +
                "        env:\n" +
                "          GITHUB_USERNAME: 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\n" +
                "          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}\n" +
                "        run: mvn deploy -Dcode.coverage=0.0 -DskipTests=true $MAVEN_CLI_OPTS\n" +
                "\n", answer);
    }

    @Test
    void unMask() {

        String answer = sut.unMask("\"${KUBERNETES_VERSION:-1.11}\"");

        assertEquals("${KUBERNETES_VERSION:-1.11}", answer);
    }
}