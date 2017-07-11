package com.infusion.relnotesgen;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infusion.relnotesgen.SCMFacade.Response;
import com.infusion.relnotesgen.util.IssueCategorizer;
import com.infusion.relnotesgen.util.JiraIssueSearchType;
import com.infusion.relnotesgen.util.JiraUtils;
import com.infusion.relnotesgen.util.ModelViewLevel;

public class ReleaseNotesModelFactoryBuilder {
    private CommitInfoProvider nestedCommitInfoProvider;
    private JiraConnector nestedJiraConnector;
    private IssueCategorizer nestedIssueCategorizer;
    private VersionInfoProvider nestedVersionInfoProvider;
    private JiraUtils nestedJiraUtils;
    private CommitMessageParser nestedCommitMessageParser;
    private SCMFacade.Response nestedGitInfo;
    private Configuration nestedConfiguration;

    
    public ReleaseNotesModelFactoryBuilder () {}
    
    public ReleaseNotesModelFactoryBuilder commitInfoProvider(final CommitInfoProvider commitInfoProvider) {
        this.nestedCommitInfoProvider = commitInfoProvider;
        return this;
    }
    
    public ReleaseNotesModelFactoryBuilder jiraConnector(final JiraConnector jiraConnector) {
        this.nestedJiraConnector = jiraConnector;
        return this;
    }
    
    public ReleaseNotesModelFactoryBuilder issueCategorizer(final IssueCategorizer issueCategorizer) {
        this.nestedIssueCategorizer = issueCategorizer;
        return this;
    }
    
    public ReleaseNotesModelFactoryBuilder versionInfoProvider(final VersionInfoProvider versionInfoProvider) {
        this.nestedVersionInfoProvider = versionInfoProvider;
        return this;
    }
    
    public ReleaseNotesModelFactoryBuilder jiraUtils(final JiraUtils jiraUtils) {
        this.nestedJiraUtils = jiraUtils;
        return this;
    }
    
    public ReleaseNotesModelFactoryBuilder commitMessageParser(final CommitMessageParser commitMessageParser) {
        this.nestedCommitMessageParser = commitMessageParser;
        return this;
    }
    
    public ReleaseNotesModelFactoryBuilder gitInfo(final SCMFacade.Response gitInfo) {
        this.nestedGitInfo = gitInfo;
        return this;
    }
    
    public ReleaseNotesModelFactoryBuilder configuration(final Configuration configuration) {
        this.nestedConfiguration = configuration;
        return this;
    }
    
    public ReleaseNotesModelFactory build() throws IllegalStateException  {
        if (!isInitalizedProperly()) {
            throw new IllegalStateException("Required parameters were not initialized");
        }
        return new ReleaseNotesModelFactory(nestedCommitInfoProvider, nestedJiraConnector, nestedIssueCategorizer, nestedVersionInfoProvider,
                nestedJiraUtils, nestedCommitMessageParser, nestedGitInfo, nestedConfiguration);
    }

    private boolean isInitalizedProperly() {
        if (nestedCommitInfoProvider==null || nestedJiraConnector==null || nestedIssueCategorizer==null || nestedVersionInfoProvider==null ||
                nestedJiraUtils==null || nestedCommitMessageParser==null || nestedGitInfo==null || nestedConfiguration==null) {
            return false;
        }
        return true;
    }       
}
