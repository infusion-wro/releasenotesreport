package com.infusion.relnotesgen;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.TagOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author trojek
 */
public class GitFacade implements SCMFacade {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.LOGGER_NAME);
    private static final String RELEASES_DIR = "releases";
    private static final String DEFAULT_VERSION = "1.0";
    private final Authenticator authenticator;

    private Git git;
    private Configuration configuration;

    public GitFacade(final Configuration configuration, Authenticator authenticator) {
        this.authenticator = authenticator;
        logger.info("Reading git repository under {}", configuration.getGitDirectory());
        this.configuration = configuration;
        try {
            File gitRepo = new File(configuration.getGitDirectory());
            if (gitRepo.exists() && searchGit(gitRepo)) {
                logger.info("Found git repository under {}", configuration.getGitDirectory());

                pull();
                fetchTags();
                checkout();
                pull();

            } else {
                logger.info("No git repository under {}", configuration.getGitDirectory());
                if (!gitRepo.exists()) {
                    logger.info("Directory {} doesn't exist, creating it...", configuration.getGitDirectory());
                    gitRepo.mkdirs();
                }

                cloneRepo();
                fetchTags();
                checkout();
                pull();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchTags() {
        logger.info("Performing fetch tags...");
        git.fetch().setTagOpt(TagOpt.FETCH_TAGS);
        logger.info("Fetch tags");
    }

    private void checkout() throws GitAPIException {
        logger.info("Git checkout to branch {}", configuration.getGitBranch());
        git.checkout().setName(configuration.getGitBranch()).call();
    }

    private void pull() throws GitAPIException, DetachedHeadException, InvalidRemoteException, CanceledException,
            RefNotFoundException, NoHeadException, TransportException {
        logger.info("Performing pull...");
        PullResult result = authenticator.authenticate(git.pull()).call();
        if (result.isSuccessful()) {
            logger.info("Pull successfull");
        } else {
            logger.warn("Pull wasn't successfull, Fetch result: {}", result.getFetchResult().getMessages());
            logger.warn("Pull wasn't successfull, Merge conflict count: {}",
                    CollectionUtils.size(result.getMergeResult().getConflicts()));
        }
    }

    private void cloneRepo() {
        logger.info("Cloning git repository url: {}", configuration.getGitUrl());

        long startTime = System.currentTimeMillis();

        final File localPath = new File(configuration.getGitDirectory());

        try {
            git = authenticator.authenticate(Git.cloneRepository()).setURI(configuration.getGitUrl())
                    .setDirectory(localPath).setBranch(configuration.getGitBranch()).setCloneAllBranches(false).call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        logger.info("Clone is done. It took {} milis.", System.currentTimeMillis() - startTime);
    }

    private boolean searchGit(final File gitRepo) throws IOException {
        logger.info("Searching for git respoitory under {}", gitRepo.getAbsolutePath());

        try {
            Repository repository = new FileRepositoryBuilder().findGitDir(gitRepo).build();
            git = new Git(repository);
            logger.info("Found git respoitory under {}", repository.getDirectory().getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.info("Didn't find git respoitory under {}", gitRepo.getAbsolutePath());
            return false;
        }
    }

    /**
     * Reads log from git
     *
     * @param commitTagRequestedLowerBound - id or tag of the older commit (bottom of the history)
     * @param commitTagRequestedUpperBound - id or tag of the newer commit (top of the history)
     * @return git commit history
     */
    @Override
    public Response readByCommit(final GitCommitTag commitTagRequestedLowerBound,
            final GitCommitTag commitTagRequestedUpperBound) {
        logger.info("Attempting to read history between '{}' and '{}'.", commitTagRequestedLowerBound,
                commitTagRequestedUpperBound);

        GitCommitTag lowerBound = commitTagRequestedLowerBound;
        GitCommitTag upperBound = commitTagRequestedUpperBound;

        // get commit for tags
        if (lowerBound.getTag() != null) {
            final String commitForTag = getCommitForTagName(lowerBound.getTag());
            lowerBound = new GitCommitTag(commitForTag, lowerBound.getTag());
        }
        // get commit for tags
        if (upperBound.getTag() != null) {
            final String commitForTag = getCommitForTagName(upperBound.getTag());
            upperBound = new GitCommitTag(commitForTag, upperBound.getTag());
        }

        if (lowerBound.getCommit() != null && upperBound.getCommit() != null) {
            // read everything in between
            return readBetweenCommits(lowerBound, upperBound);
        }
        if (commitTagRequestedLowerBound.getCommit() != null) {
            // upper bound null -> read from the latest all the way to lower bound
            return readLatestCommits(lowerBound);
        } else {
            // lower bound null -> read from the upper bound all the way to the limit
            final int limit = configuration.getGitCommitLimit();
            return readOldestCommits(upperBound, limit);
        }
    }

    private Response readOldestCommits(final GitCommitTag commitTagRequestedUpperBound, final int limit) {
        try {
            final Iterable<RevCommit> log = git.log().call();
            final Set<Commit> commits = new HashSet<>();

            RevCommit latestCommit = null;
            RevCommit oldestCommit = null;

            // commits are ordered from new to old
            for (RevCommit commit : log) {
                // skip commits that are before commit 1

                if (latestCommit == null) {
                    if (commit.getId().getName().equals(commitTagRequestedUpperBound.getCommit())) {
                        // found commit 1 - this is where history starts
                        latestCommit = commit;
                    } else {
                        // do not process commit
                        continue;
                    }
                }

                // add found commit to response set
                commits.add(new Commit(commit.getFullMessage(), commit.getId().getName(),
                        commit.getAuthorIdent().getName()));
                oldestCommit = commit;

                if (commits.size() == limit) {
                    // didn't found oldest, had to stop because of commit limit
                    logger.info("Used limit of {} commits for history and reached '{}'", limit,
                            oldestCommit.getId().getName());
                    break;
                }
            }

            logger.info("Found {} commit messages.", commits.size());
            if (commits.size() == 0) {
                throw new RuntimeException(String.format(
                        "No commit were found for given commit ids [%s, empty]. Maybe branch is badly chosen.",
                        commitTagRequestedUpperBound.getCommit()));
            }

            return new Response(commits, getVersion(latestCommit),
                    new GitCommitTag(oldestCommit.getId().getName(), null), commitTagRequestedUpperBound,
                    this.configuration.getGitBranch());
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private Response readLatestCommits(final GitCommitTag commitTagRequestedLowerBound) {
        try {
            RevCommit latestCommit = null;
            final Iterable<RevCommit> log = git.log().call();
            final Set<Commit> commits = new HashSet<>();

            // 'Requested' are commits or tags that user specified, 'Used' are the ones that were used for creating release notes
            GitCommitTag commitTagUsedUpperBound = null;

            // commits are ordered from new to old
            for (RevCommit commit : log) {
                // skip commits that are before commit 1

                if (latestCommit == null) {
                    // found commit 1 - this is where history starts
                    latestCommit = commit;
                    commitTagUsedUpperBound = new GitCommitTag(commit.getId().getName(), null);
                    logger.info("Reading history between '{}' and '{}'.", commitTagRequestedLowerBound,
                            commitTagUsedUpperBound);
                }

                // add found commit to response set
                commits.add(new Commit(commit.getFullMessage(), commit.getId().getName(),
                        commit.getAuthorIdent().getName()));

                if (commit.getId().getName().equals(commitTagRequestedLowerBound.getCommit())) {
                    // found oldest commit
                    break;
                }
            }

            logger.info("Found {} commit messages.", commits.size());
            if (commits.size() == 0) {
                throw new RuntimeException(
                        String.format("No commit were found for given commit ids %s, %s. Maybe branch is badly chosen.",
                                commitTagRequestedLowerBound.getCommit(), commitTagRequestedLowerBound.getCommit()));
            }

            return new Response(commits, getVersion(latestCommit), commitTagRequestedLowerBound,
                    commitTagUsedUpperBound, this.configuration.getGitBranch());
        } catch (GitAPIException e)

        {
            throw new RuntimeException(e);
        }
    }

    private Response readBetweenCommits(final GitCommitTag commitTagRequestedLowerBound,
            final GitCommitTag commitTagRequestedUpperBound) {
        try {
            logger.info("Reading history between '{}' and '{}'.", commitTagRequestedLowerBound,
                    commitTagRequestedUpperBound);

            final Iterable<RevCommit> log = git.log().call();
            final Set<Commit> commits = new HashSet<>();
            RevCommit latestCommitForVersion = null;

            for (RevCommit commit : log) {
                // skip commits that are before commit 1

                if (latestCommitForVersion == null) {
                    if (commit.getId().getName().equals(commitTagRequestedUpperBound.getCommit())) {
                        // found commit 1 - this is where history starts
                        latestCommitForVersion = commit;
                    } else {
                        // do not process commit
                        continue;
                    }
                }

                if (commit.getId().getName().equals(commitTagRequestedLowerBound.getCommit())) {
                    // found oldest commit which should not be included
                    break;
                }

                // add found commit to response set
                commits.add(new Commit(commit.getFullMessage(), commit.getId().getName(),
                        commit.getAuthorIdent().getName()));

            }

            logger.info("Found {} commit messages.", commits.size());
            if (commits.size() == 0) {
                throw new RuntimeException(
                        "No commit were found for given commit ids " + commitTagRequestedLowerBound.getCommit() + ", "
                                + commitTagRequestedLowerBound.getCommit() + ". Maybe branch is badly chosen.");
            }

            return new Response(commits, getVersion(latestCommitForVersion), commitTagRequestedLowerBound,
                    commitTagRequestedUpperBound, this.configuration.getGitBranch());
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads git log by commit ids
     *
     * @param commitIdLowerBound id of the older commit (bottom of the history)
     * @param commitIdUpperBound id of the newer commit (top of the history)
     * @return
     */
    @Override
    public Response readByCommit(final String commitIdLowerBound, final String commitIdUpperBound) {
        return readByCommit(new GitCommitTag(commitIdLowerBound, null), new GitCommitTag(commitIdUpperBound, null));
    }

    private String getVersion(final RevCommit commit) {
        logger.info("Searching for version in commit '{}'", commit.getFullMessage());
        try {
            logger.info("Checkout to commit '{}'", commit.getId().getName());
            git.checkout().setName(commit.getId().getName()).call();

            File pomXmlParent = git.getRepository().getDirectory().getParentFile();
            logger.info("Searching for pom.xml in directory '{}'", pomXmlParent.getAbsolutePath());

            File[] pomXmls = pomXmlParent.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(final File dir, final String name) {
                    return "pom.xml".equals(name);
                }
            });

            if (pomXmls.length == 0) {
                logger.warn("Coulnd't find pom.xml file using default version {}", DEFAULT_VERSION);
                return DEFAULT_VERSION;
            }

            String version = getVersion(pomXmls[0]);
            logger.info("Found version {} in pom.xml {}", version, pomXmls[0].getAbsolutePath());
            return version;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                checkout();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getVersion(final File pomXml)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(pomXml);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("/project/version/text()");
        return expr.evaluate(doc);
    }

    /**
     * Reads git log by commit tags
     *
     * @param tagLowerBound id of the older commit (bottom of the history)
     * @param tagUpperBound id of the newer commit (top of the history)
     * @return
     */
    @Override
    public Response readByTag(final String tagLowerBound, final String tagUpperBound) {
        try {
            Iterable<Ref> tags = git.tagList().call();

            GitCommitTag commitTag1 = GitCommitTag.Empty;
            GitCommitTag commitTag2 = GitCommitTag.Empty;

            for (Ref tag : tags) {
                if (isNotBlank(tagLowerBound) && tag.getName().endsWith(tagLowerBound)) {
                    final String commit1 = retrieveCommitIdFromTag(tag);
                    commitTag1 = new GitCommitTag(commit1, tag.getName());
                    logger.info("Found tag '{}' using commit id '{}'.", tag.getName(), commit1);
                }
                if (isNotBlank(tagUpperBound) && tag.getName().endsWith(tagUpperBound)) {
                    final String commit2 = retrieveCommitIdFromTag(tag);
                    commitTag2 = new GitCommitTag(commit2, tag.getName());
                    logger.info("Found tag '{}' using commit id '{}'.", tag.getName(), commit2);
                }
            }

            if (isNotBlank(tagLowerBound) && commitTag1.getTag() == null) {
                logger.info("Tag '{}' NOT FOUND.", tagLowerBound);
            }
            if (isNotBlank(tagUpperBound) && commitTag2.getTag() == null) {
                logger.info("Tag '{}' NOT FOUND.", tagUpperBound);
            }

            return readByCommit(commitTag1, commitTag2);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCommitForTagName(final String tagName) {
        try {
            Iterable<Ref> tags = git.tagList().call();

            for (Ref tag : tags) {
                // todo: why ends with?
                if (isNotBlank(tagName) && tag.getName().endsWith(tagName)) {
                    String commit = retrieveCommitIdFromTag(tag);
                    logger.info("Found tag '{}' using commit id '{}'.", tag.getName(), commit);

                    return commit;
                }
            }

            throw new RuntimeException("Tag '" + tagName + "' NOT FOUND.");

        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private String retrieveCommitIdFromTag(final Ref tag) {
        Ref peeledTag = git.getRepository().peel(tag);
        if (peeledTag.getPeeledObjectId() == null) {
            //http://dev.eclipse.org/mhonarc/lists/jgit-dev/msg01706.html
            //when peeled tag is null it means this is 'lighweight' tag and object id points to commit straight forward
            return peeledTag.getObjectId().getName();
        } else {
            return peeledTag.getPeeledObjectId().getName();
        }
    }

    @Override
    public Response readyTillLastTag() {
        try {
            Iterable<Ref> tags = git.tagList().call();
            Dictionary<String, Set<String>> commitsWithTags = new Hashtable<>();

            for (Ref tag : tags) {
                // todo: why ends with?
                String commit = retrieveCommitIdFromTag(tag);
                logger.info("Found tag '{}' using commit id '{}'.", tag.getName(), commit);
                if (commitsWithTags.get(commit) != null) {
                    commitsWithTags.get(commit).add(tag.getName());
                } else {
                    HashSet<String> tagsList = new HashSet<>();
                    tagsList.add(tag.getName());
                    commitsWithTags.put(commit, tagsList);
                }
            }

            // now read commits
            final Iterable<RevCommit> log = git.log().call();
            final Set<Commit> commits = new HashSet<>();
            RevCommit latestCommit = null;
            String lastTag = null;
            String lastCommit = null;

            for (RevCommit commit : log) {

                // add found commit to response set
                commits.add(new Commit(commit.getFullMessage(), commit.getId().getName(),
                        commit.getAuthorIdent().getName()));

                if (latestCommit != null) {
                    // check if we reached a tag
                    Set<String> tagsFound = commitsWithTags.get(commit.getId().getName());
                    if (tagsFound != null) {
                        lastCommit = commit.getId().getName();
                        lastTag = tagsFound.toArray()[0].toString();
                        logger.info("Found ending tag '{}' for commit {}", lastTag, lastCommit);
                        break;
                    }
                }

                // save the latest commit for version extraction
                if (latestCommit == null) {
                    latestCommit = commit;
                }
            }

            logger.info("Found {} commit messages.", commits.size());
            if (commits.size() == 0) {
                throw new RuntimeException("No commit were found. Maybe branch is badly chosen.");
            }

            return new Response(commits, getVersion(latestCommit), new GitCommitTag(lastCommit, lastTag),
                    new GitCommitTag(latestCommit.getId().getName(), null), this.configuration.getGitBranch());

        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean pushReleaseNotes(final File releaseNotes, final String version) {
        File notesDirectory = new File(git.getRepository().getDirectory().getParentFile(), RELEASES_DIR);
        boolean directoryCreated = false;
        if (!notesDirectory.exists()) {
            logger.info("Directory with release notes doesn't exist creating it in {}",
                    notesDirectory.getAbsolutePath());
            directoryCreated = notesDirectory.mkdir();
        }
        logger.info("Copying release notes to {} (will overwrite if aleady exists)", notesDirectory.getAbsolutePath());
        File releaseNotesInGit = new File(notesDirectory, releaseNotes.getName());
        try {
            Files.copy(releaseNotes.toPath(), releaseNotesInGit.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        logger.info("Pushing release notes {}", releaseNotesInGit.getAbsolutePath());
        try {
            AddCommand addCommand = git.add();
            if (directoryCreated) {
                addCommand.addFilepattern(RELEASES_DIR);
            } else {
                addCommand.addFilepattern(RELEASES_DIR + "/" + releaseNotes.getName());
            }
            addCommand.call();

            Set<String> changes = validateChangesStatusOfReleaseNotes();
            if (changes == null) {
                return false;
            }

            String commitMessage = buildCommitMessage(version);
            logger.info("Committing file '{}' with message '{}', committer name {}, committer mail {}",
                    changes.iterator().next(), commitMessage, configuration.getGitCommitterName(),
                    configuration.getGitCommitterMail());
            git.commit().setCommitter(configuration.getGitCommitterName(), configuration.getGitCommitterMail())
                    .setMessage(commitMessage).call();

            logger.info("Pushing changes to remote...");
            Iterable<PushResult> pushResults = authenticator.authenticate(git.push()).call();
            logger.info("Push call has ended.");
            for (PushResult pushResult : pushResults) {
                logger.info("Push message: {}", pushResult.getMessages());
            }
            return true;
        } catch (GitAPIException e) {
            logger.error("Error during pushing release notes", e);
            return false;
        }
    }

    private Set<String> validateChangesStatusOfReleaseNotes() throws NoWorkTreeException, GitAPIException {
        Status status = git.status().call();
        Set<String> added = status.getAdded();
        Set<String> modified = status.getModified();
        Set<String> changed = status.getChanged();
        if (added.size() > 1 || modified.size() > 1 || changed.size() > 1) {
            logger.error(
                    "There are more than one change [added({}), modified({}), changed({})] to be commited, cancelling pushing release notes.",
                    added.size(), modified.size(), changed.size());
            return null;
        }
        if (added.isEmpty() && modified.isEmpty() && changed.isEmpty()) {
            logger.error(
                    "There are no changes to be commited, probably identical release notes has been already generated and pushed to repository.");
            return null;
        }
        if (!added.isEmpty()) {
            return added;
        }
        if (!modified.isEmpty()) {
            return modified;
        }
        if (!changed.isEmpty()) {
            return changed;
        }
        return null;
    }

    private String buildCommitMessage(final String version) {
        StringBuilder messageBuilder = new StringBuilder("[release-notes-generator] Release notes for version ")
                .append(version).append(".");
        if (StringUtils.isNotEmpty(configuration.getGitCommitMessageValidationOmmitter())) {
            messageBuilder.append(" ").append(configuration.getGitCommitMessageValidationOmmitter());
        }
        return messageBuilder.toString();
    }

    @Override
    public void close() {
        git.close();
    }
}
