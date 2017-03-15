package com.infusion.relnotesgen;

import com.atlassian.jira.rest.client.api.domain.Issue;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by pkarpala on 4/9/2016.
 */
public class ReportJiraIssueModelTest {

    private Issue issue;
    private final String fixedInFlowWebVersion = "1.0";
    private final String releaseNotes = "my comment about the issue";
    private final String url = "http://dummy.com/1";
    private final String[] EmptyArray = new String[0];
    private final Iterable<String> Versions = new ArrayList<String>() {{
        add("V1");
        add("V2");
    }};

    @Before
    public void setup() {

        issue = null;

    }

    @Test
    public void reportJiraIssueModel_should_haveEmptyArray_when_defectIdNull() throws IOException {
        // Given
        String defectId = null;

        // When
        ReportJiraIssueModel model = new ReportJiraIssueModel(issue, defectId, url, fixedInFlowWebVersion, releaseNotes, Versions);

        // Then
        assertThat(model.getDefectIds(), equalTo(EmptyArray));
    }

    @Test
    public void reportJiraIssueModel_should_haveOneDefect_when_OneDefect() throws IOException {
        // Given
        final String defectId = "Defect_123";
        final String[] expected = { defectId };

        // When
        ReportJiraIssueModel model = new ReportJiraIssueModel(issue, defectId, url, fixedInFlowWebVersion, releaseNotes,new ArrayList<String>() );

        // Then
        assertArrayEquals(expected, model.getDefectIds());
    }

    @Test
    public void reportJiraIssueModel_should_haveTwoDefects_when_DefectsWithComa() throws IOException {
        // Given
        final String defectId = "Defect_123, Defect_3";
        final String[] expected = { "Defect_123", "Defect_3" };

        // When
        ReportJiraIssueModel model = new ReportJiraIssueModel(issue, defectId, url, fixedInFlowWebVersion, releaseNotes, new ArrayList<String>());

        // Then
        assertArrayEquals(expected, model.getDefectIds());
    }

    @Test
    public void reportJiraIssueModel_should_haveTwoDefects_when_DefectsWithSpace() throws IOException {
        // Given
        final String defectId = "Defect_123 Defect_3";
        final String[] expected = { "Defect_123", "Defect_3" };

        // When
        ReportJiraIssueModel model = new ReportJiraIssueModel(issue, defectId, url, fixedInFlowWebVersion, releaseNotes, Versions);

        // Then
        assertArrayEquals(expected, model.getDefectIds());
    }
}
