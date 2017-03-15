package com.infusion.relnotesgen;

import com.google.common.base.Function;
import com.google.common.collect.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ReleaseNotesModel {
    private final ImmutableSet<String> issueCategoryNames;
    private final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> issuesByCategory;
    private final ImmutableSet<ReportCommitModel> commitsWithDefectIds;
    private final String releaseVersion;
    private final SCMFacade.GitCommitTag commitTag1;
    private final SCMFacade.GitCommitTag commitTag2;
    private final int commitsCount;
    private final String gitBranch;
    private final ImmutableSortedSet<String> uniqueDefects;
    private final String jqlLink;

    public ReleaseNotesModel(final ImmutableSet<String> issueCategoryNames, final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> issuesByCategory,
            final ImmutableSet<ReportCommitModel> commitsWithDefectIds, final String releaseVersion,
            final SCMFacade.GitCommitTag commitTag1, final SCMFacade.GitCommitTag commitTag2, final int commitsCount,
            final String gitBranch) {
        this.issueCategoryNames = issueCategoryNames;
        this.issuesByCategory = issuesByCategory;
        this.commitsWithDefectIds = commitsWithDefectIds;
        this.releaseVersion = releaseVersion;
        this.commitTag1 = commitTag1;
        this.commitTag2 = commitTag2;
        this.commitsCount = commitsCount;
        this.gitBranch = gitBranch;

        uniqueDefects = FluentIterable
                .from(issuesByCategory.values())
                .transformAndConcat(new Function<ImmutableSet<ReportJiraIssueModel>, List<String>>() {

                    @Override
                    public List<String> apply(ImmutableSet<ReportJiraIssueModel> reportJiraIssueModels) {
                        return FluentIterable.from(reportJiraIssueModels)
                                .transformAndConcat(new Function<ReportJiraIssueModel, List<String>>() {

                                            @Override
                                            public List<String> apply(ReportJiraIssueModel reportJiraIssueModel) {
                                                return new ArrayList<>(
                                                        Arrays.asList(reportJiraIssueModel.getDefectIds()));
                                            }
                                        }).toList();

                    }
                }).append(FluentIterable.from(commitsWithDefectIds)
                                .transformAndConcat(new Function<ReportCommitModel, ImmutableSet<String>>() {
                                    @Override
                                    public ImmutableSet<String> apply(ReportCommitModel reportCommitModel) {
                                        return reportCommitModel.getDefectIds();
                                    }
                                })

                )
                .transform(new Function<String, String>() {

                    @Override
                    public String apply(String s) {
                        return s.toUpperCase().replace("EFECT", "efect");
                    }
                })
                .toSortedSet(new Comparator<String>() {

                                 @Override
                                 public int compare(String o1, String o2) {
                                     return o1.compareTo(o2);
                                 }
                             }

                );

        ImmutableSortedSet<String> uniqueJiras = FluentIterable
                .from(issuesByCategory.values())
                .transformAndConcat(new Function<ImmutableSet<ReportJiraIssueModel>, List<String>>() {

                    @Override
                    public List<String> apply(ImmutableSet<ReportJiraIssueModel> reportJiraIssueModels) {
                        return FluentIterable.from(reportJiraIssueModels)
                                .transform(new Function<ReportJiraIssueModel, String>() {

                                    @Override
                                    public String apply(ReportJiraIssueModel reportJiraIssueModel) {
                                        return reportJiraIssueModel.getIssue().getKey();
                                    }
                                }).toList();

                    }
                })
                .toSortedSet(new Comparator<String>() {

                                 @Override
                                 public int compare(String o1, String o2) {
                                     return o1.compareTo(o2);
                                 }
                             }

                );

        StringBuilder sb = new StringBuilder("https://ensemble.atlassian.net/issues/?jql=id%20in%20(");
        final String coma = "%2C%20";
        for (String s : uniqueJiras)
        {
            sb.append(s);
            sb.append(coma);
        }
        sb.replace(sb.length()-coma.length(),sb.length(),"");
        sb.append(")");

        jqlLink = sb.toString();
    }

    public ImmutableSet<String> getIssueCategoryNames() {
        return issueCategoryNames;
    }

    public ImmutableSet<ReportJiraIssueModel> getIssuesByCategoryName(String categoryName) {
        return issuesByCategory.get(categoryName);
    }

    public ImmutableSet<ReportCommitModel> getCommitsWithDefectIds() {
        return commitsWithDefectIds;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public SCMFacade.GitCommitTag getCommitTag1() {
        return commitTag1;
    }

    public SCMFacade.GitCommitTag getCommitTag2() {
        return commitTag2;
    }

    public int getCommitsCount() {
        return commitsCount;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public ImmutableSortedSet<String> getUniqueDefects() {
        return uniqueDefects;
    }

    public String getJqlLink() {return jqlLink; }
}
