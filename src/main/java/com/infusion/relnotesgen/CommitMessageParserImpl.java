package com.infusion.relnotesgen;

import com.google.common.collect.ImmutableSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitMessageParserImpl implements CommitMessageParser {
    private final Pattern jiraKeyPattern;
    private final Pattern defectIdPattern; // TODO Pattern.compile("((HA)|(CP))-\\\\d+");

    public CommitMessageParserImpl(final Configuration configuration) {
        jiraKeyPattern = Pattern.compile(configuration.getJiraIssuePattern());
        defectIdPattern = Pattern.compile("((defect_)|(FSU-)|(CR_CR)|(CR_FOR)|(R2REQ)|(R3REQ))\\d+", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public ImmutableSet<String> getJiraKeys(final String text) {
        return matchAll(jiraKeyPattern, text);
    }

    @Override
    public ImmutableSet<String> getDefectIds(final String text) {
        return matchAll(defectIdPattern, text);
    }

    private ImmutableSet<String> matchAll(final Pattern pattern, final String text) {
        ImmutableSet.Builder<String> matchesBuilder = ImmutableSet.builder();
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            matchesBuilder.add(matcher.group());
        }

        return matchesBuilder.build();
    }
}
