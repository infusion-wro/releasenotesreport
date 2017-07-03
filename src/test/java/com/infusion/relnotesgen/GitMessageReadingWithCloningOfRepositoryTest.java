package com.infusion.relnotesgen;

import com.infusion.relnotesgen.util.TestGitRepo;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import static com.infusion.relnotesgen.util.TestUtil.getMessages;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * @author trojek
 *
 */
public class GitMessageReadingWithCloningOfRepositoryTest {


    private static TestGitRepo testGitRepo = new TestGitRepo();

    private GitFacade gitMessageReader;
    private File tempRepo;

    @Before
    public void cloneRepo() throws IOException {
        tempRepo = Files.createTempDirectory("TestCloneGitRepo").toFile();
    }

    @After
    public void cleanRepo() throws IOException {
        gitMessageReader.close();
        FileUtils.deleteDirectory(tempRepo);
    }

    @AfterClass
    public static void removeTestGitRepo() throws IOException {
        testGitRepo.clean();
    }

    @Test
    public void cloneNewRepositoryWithBranchNameGiven() throws IOException {
        // Given
        String commitId1 = "2ea0809c55657bc528933e6fda3a7772cacf8279";
        String commitId2 = "2ea0809c55657bc528933e6fda3a7772cacf8279";
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .branch("branch1")
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        Set<String> messages = getMessages(gitMessageReader.readByCommit(commitId1, commitId2).commits);

        // Then
        assertThat(messages, hasSize(1));
        assertThat(messages, hasItems("SYM-4 changed dummy file on branch1 branch\n"));
    }

    @Test
    public void cloneNewRepositoryWithoutBranchNameGiven() throws IOException {
        // Given
        String commitId1 = "33589445102fd7b49421006e0447836429d84113";
        String commitId2 = "948fa8f6cc8a49f08e3c3a426c9e3d7323ce469a";
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        Set<String> messages = getMessages(gitMessageReader.readByCommit(commitId1, commitId2).commits);

        // Then
        assertThat(messages, hasSize(1));
        assertThat(messages, hasItems("SYM-2 changed dummy file for second time\n"));
    }

    @Test
    public void readWithOneCommit() throws IOException {
        // Given
        String commitId1 = "33589445102fd7b49421006e0447836429d84113";
        String commitId2 = "948fa8f6cc8a49f08e3c3a426c9e3d7323ce469a";
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        Set<String> messages = getMessages(gitMessageReader.readByCommit(commitId1, commitId2).commits);

        // Then
        assertThat(messages, hasSize(1));
        assertThat(messages, hasItems("SYM-2 changed dummy file for second time\n"));
    }

    @Test
    public void readVersionFromPomXml() throws IOException {
        // Given
        String commitId1 = "1c814546893dc5544f86ca87ca58f0d162c9ccd2";
        String commitId2 = "50dbc466d1fa6ddc714ebabbeae585af7a72524b";
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        String version = gitMessageReader.readByCommit(commitId1, commitId2).version;

        // Then
        assertThat(version, Matchers.equalTo("1.1"));
    }

    @Test
    public void readVersionFromPomXmlSnapshot() throws IOException {
        // Given
        String commitId1 = "1c814546893dc5544f86ca87ca58f0d162c9ccd2";
        String commitId2 = "4f4685dfcff6514558f08d3dd303bda4684f0ffd";
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        String version = gitMessageReader.readByCommit(commitId1, commitId2).version;

        // Then
        assertThat(version, Matchers.equalTo("1.1-SNAPSHOT"));
    }

    @Test
    public void readByTagWithTwoNeighbourTags() {
        // Given
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();

        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        SCMFacade.Response gitInfo = gitMessageReader.readByTag("1.2", "1.3");
        Set<String> messages = getMessages(gitInfo.commits);

        // Then
        assertThat(messages, hasSize(3));
        assertThat(messages, hasItems("SYM-33 release of version 1.3\n", "SYM-31 prepare for version 1.3\n", "SYM-32 prepare for version 1.3 part 2\n", "SYM-33 release of version 1.3\n"));
        assertThat(gitInfo.version, equalTo("1.3"));
    }

    @Test
    public void readByTagWithTwoTagsHavingOtherTagBetweenThem() {
        // Given
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        SCMFacade.Response gitInfo = gitMessageReader.readByTag("1.1", "1.4");
        Set<String> messages = getMessages(gitInfo.commits);

        // Then
        assertThat(messages, hasSize(9));
        assertThat(messages, hasItems("SYM-31 prepare for version 1.3\n", "SYM-32 prepare for version 1.3 part 2\n", "SYM-33 release of version 1.3\n",
                "SYM-41 prepare for version 1.4\n", "SYM-42 prepare for version 1.4 part 2\n", "SYM-43 releas of version 1.4\n"));
        assertThat(gitInfo.version, equalTo("1.4"));
    }

    @Test
    public void readByTagWithOneTag() {
        // Given
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
        SCMFacade.Response gitInfo = gitMessageReader.readByTag("1.3", null);
        Set<String> messages = getMessages(gitInfo.commits);

        // Then
        assertThat(messages, hasSize(4));
        assertThat(messages, hasItems("SYM-33 release of version 1.3\n", "SYM-41 prepare for version 1.4\n", "SYM-42 prepare for version 1.4 part 2\n", "SYM-43 releas of version 1.4\n"));
        assertThat(gitInfo.version, equalTo("1.4"));
    }

    @Test
    public void readLatestReleasedVersionWhichMeansReadByTwoLatestTags() {
        // Given
        Configuration conf = testGitRepo.configuration()
                .gitDirectory(tempRepo.getAbsolutePath())
                .build();
        gitMessageReader = new GitFacade(conf, new UserCredentialsAuthenticator(conf));

        // When
//        SCMFacade.Response gitInfo = gitMessageReader.readLatestReleasedVersion();
        SCMFacade.Response gitInfo = gitMessageReader.readyTillLastTag();
        Set<String> messages = getMessages(gitInfo.commits);

        // Then
        assertThat(messages, hasSize(4));
        assertThat(messages, hasItems("SYM-33 release of version 1.3\n", "SYM-41 prepare for version 1.4\n", "SYM-42 prepare for version 1.4 part 2\n", "SYM-43 releas of version 1.4\n"));
        assertThat(gitInfo.version, equalTo("1.4"));
    }
}
