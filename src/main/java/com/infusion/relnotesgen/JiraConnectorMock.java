package com.infusion.relnotesgen;

import com.atlassian.jira.rest.client.api.domain.Issue;

import com.google.common.collect.*;
import com.infusion.relnotesgen.util.FileUtils;
import com.infusion.relnotesgen.util.JiraIssueSearchType;
import com.infusion.relnotesgen.util.XstreamSerializer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class JiraConnectorMock implements JiraConnector {
	private final static Logger logger = LoggerFactory.getLogger(Configuration.LOGGER_NAME);
    private final Configuration configuration;
    private final String ISSUES_INCLUDE_PARENTS_FILE = "issuesIncludeParents.xml";
    private final String ISSUES_BY_FIX_VERSIONS_FILE = "issuesByFixVersions.xml";
    private final String KNOWN_ISSUES_BY_JQL_FILE = "knownIssuesByJql.xml";

    public JiraConnectorMock(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
	public ImmutableMap<String, Issue> getIssuesIncludeParents(final ImmutableSet<String> issueIds, final Map<JiraIssueSearchType, String> errors) {
        Map<String, Issue> returnVal = null;
        try {
            String filename = FileUtils.generateSerializedObjectPath(configuration.getReportDirectory()) + ISSUES_INCLUDE_PARENTS_FILE;
            File objXmlFile = new File(filename);
            XstreamSerializer<ImmutableMap<String, Issue>> serializer = new XstreamSerializer<ImmutableMap<String, Issue>>();
            if(objXmlFile.exists() && !objXmlFile.isDirectory()) { 
                // deserialize and return
                returnVal = serializer.deserialize(filename);
            } else {
                // retrieve, serialize and return
                JiraConnector jiraConnector = new JiraConnectorImpl(configuration);
                returnVal = jiraConnector.getIssuesIncludeParents(issueIds, errors);
                serializer.serialize(filename, (ImmutableMap<String, Issue>) returnVal);
            }

        } catch (IOException e) {
            throw new RuntimeException("Exception while contacting JIRA", e);
        }
        return ImmutableMap.copyOf(returnVal);
    }

    @Override
	public ImmutableMap<String, Issue> getIssuesByFixVersions(final ImmutableSet<String> fixVersions, final Map<JiraIssueSearchType, String> errors) {
        Map<String, Issue> returnVal = null;
        try {
            String filename = FileUtils.generateSerializedObjectPath(configuration.getReportDirectory()) + ISSUES_BY_FIX_VERSIONS_FILE;
            File objXmlFile = new File(filename);
            XstreamSerializer<ImmutableMap<String, Issue>> serializer = new XstreamSerializer<ImmutableMap<String, Issue>>();
            if(objXmlFile.exists() && !objXmlFile.isDirectory()) { 
                // deserialize and return
                returnVal = serializer.deserialize(filename);
            } else {
                // retrieve, serialize and return
                JiraConnector jiraConnector = new JiraConnectorImpl(configuration);
                returnVal = jiraConnector.getIssuesByFixVersions(configuration.getFixVersionsSet(), errors);
                serializer.serialize(filename, (ImmutableMap<String, Issue>) returnVal);
            }

        } catch (IOException e) {
            throw new RuntimeException("Exception while contacting JIRA", e);
        }
        return ImmutableMap.copyOf(returnVal);
	}

	@Override
	public ImmutableMap<String, Issue> getKnownIssuesByJql(final String jqlQuery, final Map<JiraIssueSearchType, String> errors) {
	    // getKnownIssuesByJql(configuration.getKnownIssues(), errors
        Map<String, Issue> returnVal = null;
        try {
            String filename = FileUtils.generateSerializedObjectPath(configuration.getReportDirectory()) + KNOWN_ISSUES_BY_JQL_FILE;
            File objXmlFile = new File(filename);
            XstreamSerializer<ImmutableMap<String, Issue>> serializer = new XstreamSerializer<ImmutableMap<String, Issue>>();
            if(objXmlFile.exists() && !objXmlFile.isDirectory()) { 
                // deserialize and return
                returnVal = serializer.deserialize(filename);
            } else {
                // retrieve, serialize and return
                JiraConnector jiraConnector = new JiraConnectorImpl(configuration);
                returnVal = jiraConnector.getKnownIssuesByJql(configuration.getKnownIssues(), errors);
                serializer.serialize(filename, (ImmutableMap<String, Issue>) returnVal);
            }

        } catch (IOException e) {
            throw new RuntimeException("Exception while contacting JIRA", e);
        }
        return ImmutableMap.copyOf(returnVal);
    }
}
