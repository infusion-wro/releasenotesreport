package com.infusion.relnotesgen;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import java.util.Arrays;

public class ReportJiraIssueModel {

    private final Issue issue;
    private final String fixedInFlowWebVersion;
    private final String releaseNotes;
    private final String[] defectIds;
    private final String url;
    private final String fixVersions;

    public ReportJiraIssueModel(final Issue issue, final String defectId, final String url,
            final String fixedInFlowWebVersion, final String releaseNotes, final Iterable<String> jiraFixVersions) {
        this.issue = issue;
        this.fixedInFlowWebVersion = fixedInFlowWebVersion;
        this.url = url;
        this.releaseNotes = releaseNotes;

        if (defectId != null) {
            defectIds = FluentIterable.from(Arrays.asList(defectId.split("(,)|( )")))
                    .transform(new Function<String, String>() {

                        @Override
                        public String apply(String s) {
                            return s.trim();
                        }
                    }).filter(new com.google.common.base.Predicate<String>() {

                        @Override
                        public boolean apply(String defect) {
                            return !defect.isEmpty();
                        }
                    }).toArray(String.class);
        } else {
            defectIds = new String[0];
        }

        StringBuilder sb = new StringBuilder();
        for(String v : jiraFixVersions) {
            sb.append(v);
            sb.append(", ");
        }

        fixVersions = sb.toString();
    }

    public Issue getIssue() {
        return issue;
    }

    public String[] getDefectIds() {
        return defectIds;
    }

    public String getUrl() {
        return url;
    }

    public String getFixedInFlowWebVersion() {
        return fixedInFlowWebVersion;
    }

    public String getReleaseNotes() { return releaseNotes; }

    public String getFixVersions() { return fixVersions; }
}
