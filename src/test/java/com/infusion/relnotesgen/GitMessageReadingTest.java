package com.infusion.relnotesgen;

import com.infusion.relnotesgen.util.TestGitRepo;
import com.infusion.relnotesgen.util.TestUtil;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author trojek
 *
 */
@RunWith(Parameterized.class)
public class GitMessageReadingTest {

    private static TestGitRepo testGitRepo = new TestGitRepo();
    private static Configuration conf = testGitRepo.configuration().build();
    private static Authenticator authenticator = new UserCredentialsAuthenticator(conf);
    private static GitFacade gitMessageReader = new GitFacade(conf, authenticator);

    private String commitId1;
    private String commitId2;
    private String[] messages;
    private String version;
    private final String testMessage;

    // test repo contents
    /*
    commit 1a90539f98c6699cc100d811fb3edac73fba9748 (HEAD, tag: refs/tags/1.4, refs/heads/master)
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:31:43 2015 +0200

    SYM-43 releas of version 1.4

commit 043d9b3fcac01a1aff6d78e7407babfb1d3d3f92
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:31:07 2015 +0200

    SYM-42 prepare for version 1.4 part 2

commit 8d2c247b31b6c94d92fb71b426e4ec168659d671
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:30:52 2015 +0200

    SYM-41 prepare for version 1.4

commit 459643f30fea11f0e0e2791c5b8b247c19df8eca (tag: refs/tags/1.3)
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:30:12 2015 +0200

    SYM-33 release of version 1.3

commit 66409e117910954121c60da11e25540ffb930a42
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:29:34 2015 +0200

    SYM-32 prepare for version 1.3 part 2

commit f911f2f4db67fec386190df1abb0a3c38b457358
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:29:15 2015 +0200

    SYM-31 prepare for version 1.3

commit 7de1590b7d9375329a85d50f916ed91faeb0622d (tag: refs/tags/1.2)
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:28:27 2015 +0200

    SYM-22 release of version 1.2

commit cbeb59384db739c9b9ee3608ca1fba451857a394
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:27:59 2015 +0200

    SYM-22 changes for version 1.2

commit dcdba9606ad8ace8c569fb46e6ba41d5ca3b7810
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 22 12:27:43 2015 +0200

    SYM-20 changes for version 1.2

commit 50dbc466d1fa6ddc714ebabbeae585af7a72524b (tag: refs/tags/1.1)
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Tue Apr 21 13:05:17 2015 +0200

    SYM-13 release of version 1.1

commit 4f4685dfcff6514558f08d3dd303bda4684f0ffd
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Tue Apr 21 13:04:47 2015 +0200

    SYM-12 version 1.1-SNAPSHOT

commit 5967dfe081ada684f45b530fe68fcb8635e2e6de
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Tue Apr 21 13:02:57 2015 +0200

    SYM-11 release of version 1.0

commit 94679b1e5d2664530e880e22fe5ef60b8d699ef5
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Tue Apr 21 13:02:27 2015 +0200

    SYM-10 added pom.xml

commit 430c3f94a1f2dd4940c547ae8a5ede83910597b9
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 15 15:40:25 2015 +0200

    SYM-3 changed dummy file for third time

commit 948fa8f6cc8a49f08e3c3a426c9e3d7323ce469a
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 15 15:39:59 2015 +0200

    SYM-2 changed dummy file for second time

commit 33589445102fd7b49421006e0447836429d84113
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 15 15:39:18 2015 +0200

    SYM-2 changed dummy file for first time

commit 1c814546893dc5544f86ca87ca58f0d162c9ccd2
Author: Tomasz Rojek <trojek@infusion.com>
Date:   Wed Apr 15 15:37:44 2015 +0200

    SYM-1 created initial dummy file
     */

    public GitMessageReadingTest(final String commitIdLowerBound, final String commitIdUpperBound, final String[] messages, final String version, final String testMessage) {
        this.commitId1 = commitIdLowerBound;
        this.commitId2 = commitIdUpperBound;
        this.messages = messages;
        this.version = version;
        this.testMessage = testMessage;
    }

    @Parameters(name = "{index}: search limited by {0} and {1} should give {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                //all commits
                { "1c814546893dc5544f86ca87ca58f0d162c9ccd2", "430c3f94a1f2dd4940c547ae8a5ede83910597b9",
                    new String[] {"SYM-2 changed dummy file for first time\n",
                        "SYM-2 changed dummy file for second time\n", "SYM-3 changed dummy file for third time\n"},
                    "1.0", "Should return 3 messages when commit 1 newer then commit 2"},

                //all commits with reverted search parameters order (430c is newer than 1c81)
                { "430c3f94a1f2dd4940c547ae8a5ede83910597b9", "1c814546893dc5544f86ca87ca58f0d162c9ccd2",
                    new String[] {"SYM-1 created initial dummy file\n"},
                        "1.0", "Should return only 1 message when provided commit ids are in the wrong order. 1c81 is the last one so only 1c81 is returned" },

                //one message
                { "948fa8f6cc8a49f08e3c3a426c9e3d7323ce469a", "948fa8f6cc8a49f08e3c3a426c9e3d7323ce469a",
                    new String[] {"SYM-2 changed dummy file for second time\n"},
                    "1.0", "Should return 1 message when start and end are equal" },

                //subcollection of elements
                { "1c814546893dc5544f86ca87ca58f0d162c9ccd2", "948fa8f6cc8a49f08e3c3a426c9e3d7323ce469a",
                    new String[] {"SYM-2 changed dummy file for first time\n",
                        "SYM-2 changed dummy file for second time\n"},
                        "1.0", "" },

                //subcollection of elements with reversed search parameters order
                { "430c3f94a1f2dd4940c547ae8a5ede83910597b9", "948fa8f6cc8a49f08e3c3a426c9e3d7323ce469a",
                    new String[] {"SYM-2 changed dummy file for first time\n", "SYM-2 changed dummy file for second time\n" ,"SYM-1 created initial dummy file\n"},
                    "1.0","Should return 3 messages since provided commits are in wrong order, so 3 commits older than 948f are returned" },

                //read by one commit second parameter null
                { "459643f30fea11f0e0e2791c5b8b247c19df8eca", null,
                    new String[] {"SYM-33 release of version 1.3\n", "SYM-41 prepare for version 1.4\n",
                        "SYM-42 prepare for version 1.4 part 2\n", "SYM-43 releas of version 1.4\n"},
                        "1.4","Since only Old commit provided, returning history all the way to the first one with respect to tbe limit" },

                //read by one commit first parameter null
                { null, "459643f30fea11f0e0e2791c5b8b247c19df8eca",
                    new String[] {"SYM-33 release of version 1.3\n", "SYM-32 prepare for version 1.3 part 2\n",
                        "SYM-31 prepare for version 1.3\n", "SYM-22 release of version 1.2\n", "SYM-22 changes for version 1.2\n"},
                        "1.3","Since only New commit provided, using the limit and returning 5 older commits" }
        });
    }

    @AfterClass
    public static void removeTestGitRepo() throws IOException {
        gitMessageReader.close();
        testGitRepo.clean();
    }

    @Test
    public void readsCommitMessagesLimitedByTwoCommitIds() {
        // Given commitId1 and commitId2

        // When
        SCMFacade.Response gitInfo = gitMessageReader.readByCommit(commitId1, commitId2);
        Set<String> messages = TestUtil.getMessages(gitInfo.commits);

        // Then
        assertThat(testMessage, messages, hasSize(this.messages.length));
        assertThat(testMessage, messages, hasItems(this.messages));
        assertThat(gitInfo.version, equalTo(this.version));
    }
}
