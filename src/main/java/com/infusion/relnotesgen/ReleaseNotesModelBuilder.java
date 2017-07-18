package com.infusion.relnotesgen;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infusion.relnotesgen.util.JiraIssueSearchType;

public class ReleaseNotesModelBuilder {

    ImmutableSet<String> nestedIssueCategoryNames;
    ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> nestedIssuesByCategory;
    ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> nestedInternalIssuesByCategory;
    ImmutableSet<ReportCommitModel> nestedCommitsWithDefectIds;
    ImmutableSet<ReportJiraIssueModel> nestedKnownIssues;
    String nestedReleaseVersion;
    SCMFacade.GitCommitTag nestedCommitTag1;
    SCMFacade.GitCommitTag nestedCommitTag2;
    int nestedCommitsCount;
    String nestedGitBranch;
    Configuration nestedConfiguration;
    Map<JiraIssueSearchType,String> nestedErrors;
    
    public ReleaseNotesModelBuilder() {}
    
    public ReleaseNotesModelBuilder issueCategoryNames(final ImmutableSet<String> issueCategoryNames) {
        this.nestedIssueCategoryNames = issueCategoryNames;
        return this;
    }

    public ReleaseNotesModelBuilder issuesByCategory(final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> issuesByCategory) {
        this.nestedIssuesByCategory = issuesByCategory;
        return this;
    }

    public ReleaseNotesModelBuilder internalIssuesByCategory(final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> internalIssuesByCategory) {
        this.nestedInternalIssuesByCategory = internalIssuesByCategory;
        return this;
    }

    public ReleaseNotesModelBuilder commitsWithDefectIds(final ImmutableSet<ReportCommitModel> commitsWithDefectIds) {
        this.nestedCommitsWithDefectIds = commitsWithDefectIds;
        return this;
    }

    public ReleaseNotesModelBuilder knownIssues(final ImmutableSet<ReportJiraIssueModel> knownIssues) {
        this.nestedKnownIssues = knownIssues;
        return this;
    }

    public ReleaseNotesModelBuilder releaseVersion(final String releaseVersion) {
        this.nestedReleaseVersion = releaseVersion;
        return this;
    }

    public ReleaseNotesModelBuilder commitTag1(final SCMFacade.GitCommitTag commitTag1) {
        this.nestedCommitTag1 = commitTag1;
        return this;
    }

    public ReleaseNotesModelBuilder commitTag2(final SCMFacade.GitCommitTag commitTag2) {
        this.nestedCommitTag2 = commitTag2;
        return this;
    }

    public ReleaseNotesModelBuilder commitsCount(final int commitsCount) {
        this.nestedCommitsCount = commitsCount;
        return this;
    }

    public ReleaseNotesModelBuilder gitBranch(final String gitBranch) {
        this.nestedGitBranch = gitBranch;
        return this;
    }

    public ReleaseNotesModelBuilder configuration(final Configuration configuration) {
        this.nestedConfiguration = configuration;
        return this;
    }
    
    public ReleaseNotesModelBuilder errors(final Map<JiraIssueSearchType,String> errors) {
        this.nestedErrors = errors;
        return this;
    }

    public ReleaseNotesModel build() throws IllegalStateException {
        if (!isInitalizedProperly()) {
            throw new IllegalStateException("Required parameters were not initialized");
        }
        if (nestedInternalIssuesByCategory == null) {
            nestedInternalIssuesByCategory = ImmutableMap.copyOf(new HashMap<String, ImmutableSet<ReportJiraIssueModel>>());
        }
        return new ReleaseNotesModel(nestedIssueCategoryNames, nestedIssuesByCategory, nestedInternalIssuesByCategory, 
                nestedCommitsWithDefectIds, nestedKnownIssues, nestedReleaseVersion, nestedCommitTag1, nestedCommitTag2, 
                nestedCommitsCount, nestedGitBranch, nestedConfiguration, nestedErrors);
    }
    
    private boolean isInitalizedProperly() {
        if (nestedIssueCategoryNames==null || nestedIssuesByCategory==null || nestedCommitsWithDefectIds==null || 
                nestedKnownIssues==null || nestedReleaseVersion==null || nestedCommitTag1==null || nestedCommitTag2==null 
                || nestedGitBranch==null || nestedConfiguration==null || nestedErrors==null) {
            return false;
        }
        return true;
    }
}