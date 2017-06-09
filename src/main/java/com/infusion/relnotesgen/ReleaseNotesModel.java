package com.infusion.relnotesgen;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.infusion.relnotesgen.SCMFacade.GitCommitTag;
import com.infusion.relnotesgen.util.JiraIssueSearchType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleaseNotesModel {
    public static final String LOGGER_NAME = "com.infusion.relnotesgen.log.ReleaseNotesLogger";
    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private static final String URL_QUOTE = "%22";
    private static final String URL_COMMA = "%2C";
    private static final String URL_SPACE = "%20";
    private static final String ISSUES_JQL_URL = "/issues/?jql=";
    private static final String JQL_BY_ID_URL = "id%20in%20(";
    private static final String URL_COMMA_AND_SPACE = URL_COMMA + URL_SPACE;
    
    public ImmutableSet<String> issueCategoryNames;
    public ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> externalIssuesByCategory;
    public ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> internalIssuesByCategory;
    public ImmutableSet<ReportCommitModel> commitsWithDefectIds;
    public String releaseVersion;
    public GitCommitTag commitTag1;
    public GitCommitTag commitTag2;
    public int commitsCount;
    public String gitBranch;
    public Configuration configuration;
    public ImmutableSortedSet<String> uniqueDefects;
    public String jqlLink;
    public String knownIssuesJqlLink;
    public ImmutableSet<String> fixVersions;
    public ImmutableSet<ReportJiraIssueModel> knownIssues;
    public Map<JiraIssueSearchType, String> errors;
    
    ReleaseNotesModel(final ImmutableSet<String> issueCategoryNames, final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> externalIssuesByCategory,
                             ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> internalIssuesByCategory, final ImmutableSet<ReportCommitModel> commitsWithDefectIds, final ImmutableSet<ReportJiraIssueModel> knownIssues, 
                             final String releaseVersion, final SCMFacade.GitCommitTag commitTag1, final SCMFacade.GitCommitTag commitTag2, final int commitsCount,
                             final String gitBranch, Configuration configuration, final Map<JiraIssueSearchType,String> errors) {
        this.issueCategoryNames = issueCategoryNames;
        this.externalIssuesByCategory = externalIssuesByCategory;
        this.internalIssuesByCategory = internalIssuesByCategory;
        this.commitsWithDefectIds = commitsWithDefectIds;
        this.releaseVersion = releaseVersion;
        this.commitTag1 = commitTag1;
        this.commitTag2 = commitTag2;
        this.commitsCount = commitsCount;
        this.gitBranch = gitBranch;
        this.configuration = configuration;
        this.fixVersions = configuration.getFixVersionsSet();
        this.knownIssues = knownIssues;
        this.errors = errors;

        uniqueDefects = generateUniqueDefects(generateValidIssuesByCategory(externalIssuesByCategory), 
                generateValidIssuesByCategory(internalIssuesByCategory), commitsWithDefectIds);
        ImmutableSortedSet<String> uniqueJiras = generateUniqueJiras(externalIssuesByCategory, internalIssuesByCategory);

        jqlLink = generateUrlEncodedJqlString(generateJqlUrl(uniqueJiras));
        knownIssuesJqlLink = generateUrlEncodedJqlString(generateJqlUrl(configuration.getKnownIssues()));
    }

    private ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> generateValidIssuesByCategory (ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> issuesByCategory) {
        Map<String, ImmutableSet<ReportJiraIssueModel>> validIssuesByCategoryTemp = new HashMap<String, ImmutableSet<ReportJiraIssueModel>>();
        validIssuesByCategoryTemp.putAll(issuesByCategory);
        for (JiraIssueSearchType curr : JiraIssueSearchType.values()) {
            if (!curr.isValid()) {
                validIssuesByCategoryTemp.remove(curr.title());
            }
        }
        return ImmutableMap.copyOf(validIssuesByCategoryTemp);
    }

    private ImmutableSortedSet<String> generateUniqueJiras(final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> externalIssuesByCategory, ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> internalIssuesByCategory) {
        ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> merged = generateMergedMaps(externalIssuesByCategory, internalIssuesByCategory);
        return FluentIterable
                .from(merged.values())
                .transformAndConcat(new Function<ImmutableSet<ReportJiraIssueModel>, List<String>>() {

                    @Override
                    public List<String> apply(final ImmutableSet<ReportJiraIssueModel> reportJiraIssueModels) {
                        return FluentIterable.from(reportJiraIssueModels)
                            .transform(new Function<ReportJiraIssueModel, String>() {

                                @Override
                                public String apply(final ReportJiraIssueModel reportJiraIssueModel) {
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
    }

    private ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> generateMergedMaps(
            final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> externalIssuesByCategory,
            final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> internalIssuesByCategory) {
        Map<String, Set<ReportJiraIssueModel>> merged = new HashMap<String, Set<ReportJiraIssueModel>>();

        // put all entries from external into the merged set
        for ( Map.Entry<String, ImmutableSet<ReportJiraIssueModel>> entry : externalIssuesByCategory.entrySet() ) {
            Set<ReportJiraIssueModel> temp = new HashSet<ReportJiraIssueModel>();
            temp.addAll(entry.getValue());
            merged.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        
        // put all entries from internal into the merged set
        for ( Map.Entry<String, ImmutableSet<ReportJiraIssueModel>> entry : internalIssuesByCategory.entrySet() ) {
            if (merged.containsKey(entry.getKey())) {
                merged.get(entry.getKey()).addAll(entry.getValue());
            } else {
                Set<ReportJiraIssueModel> temp = new HashSet<ReportJiraIssueModel>();
                temp.addAll(entry.getValue());
                merged.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }

        // convert from Set to ImmutableSet
        Map<String, ImmutableSet<ReportJiraIssueModel>> mergedTemp = new HashMap<String, ImmutableSet<ReportJiraIssueModel>>();
        for (Map.Entry<String,Set<ReportJiraIssueModel>> entry : merged.entrySet()) {
            mergedTemp.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));            
        }
        
        // convert to ImmutableMap
        return ImmutableMap.copyOf(mergedTemp);
    }

    private ImmutableSortedSet<String> generateUniqueDefects(final ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> externalIssuesByCategory,
            ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> internalIssuesByCategory, final ImmutableSet<ReportCommitModel> commitsWithDefectIds) {

        ImmutableMap<String, ImmutableSet<ReportJiraIssueModel>> merged = generateMergedMaps(externalIssuesByCategory, internalIssuesByCategory);
        return FluentIterable
                .from(merged.values())
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
                })
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
    }

    private String generateUrlEncodedJqlString(final String jqlString) {
        StringBuilder sb = new StringBuilder(configuration.getJiraUrl());
        sb.append(ISSUES_JQL_URL);
        sb.append(jqlString);
        return sb.toString();
    }

    private String generateJqlUrl(final String knownIssues) {
        if (knownIssues==null || knownIssues.isEmpty()) {
            return "";
        }
        return knownIssues.replaceAll(",", URL_COMMA).replaceAll(" ", URL_SPACE).replaceAll("\"", URL_QUOTE);
    }

    private String generateJqlUrl(final ImmutableSortedSet<String> uniqueJiras) {
        StringBuilder sb = new StringBuilder(JQL_BY_ID_URL);
        for (String s : uniqueJiras) {
            sb.append(s);
            sb.append(URL_COMMA_AND_SPACE);
        }
        sb.replace(sb.length()-URL_COMMA_AND_SPACE.length(),sb.length(),"");
        sb.append(")");
        return sb.toString();
    }

    public ImmutableSet<String> getIssueCategoryNames() {
        return issueCategoryNames;
    }
    
    public boolean categoryNameIsInvalid(final String categoryName) {
        for (JiraIssueSearchType curr : JiraIssueSearchType.values()) {
            if (curr.title().equals(categoryName) && !curr.isValid()) {
                return true;
            }
        }
        return false;    
    }

    public List<String> getIssueCategoryNamesList() {
        List<String> sortedList = new ArrayList<String>();
        for (String categoryName : issueCategoryNames) {
            if (JiraIssueSearchType.INVALID_STATE.title().equals(categoryName)) {
                sortedList.add(0, categoryName);
            } else if (JiraIssueSearchType.INVALID_FIX_VERSION.title().equals(categoryName)) {
                sortedList.add(0, categoryName);
            } else {
                sortedList.add(categoryName);
            }
        }
        return sortedList;
    }

    public int getTotalInvalidIssueCount() {
        return getTotalExternalInvalidIssueCount() + getTotalInternalInvalidIssueCount();
    }
    
    public int getTotalExternalInvalidIssueCount() {
        int invalidCount = 0;
        for (JiraIssueSearchType curr : JiraIssueSearchType.values()) {
            if (!curr.isValid()) {
                invalidCount += getExternalIssueCountByCategoryName(curr.title());
            }
        }
        return invalidCount;
    }

    public int getTotalInternalInvalidIssueCount() {
        int invalidCount = 0;
        for (JiraIssueSearchType curr : JiraIssueSearchType.values()) {
            if (!curr.isValid()) {
                invalidCount += getInternalIssueCountByCategoryName(curr.title());
            }
        }
        return invalidCount;
    }

    public int getTotalIssueCountByCategoryName(final String categoryName) {
        try {
            return getExternalIssuesByCategoryName(categoryName).size() + getInternalIssuesByCategoryName(categoryName).size();
        } catch (Exception e) {
            logger.warn("{}", e.getMessage(), e);
            return 0;
        }
    }

    public int getExternalIssueCountByCategoryName(final String categoryName) {
        try {
            return getExternalIssuesByCategoryName(categoryName).size();
        } catch (Exception e) {
            logger.warn("{}", e.getMessage(), e);
            return 0;
        }
    }

    public int getInternalIssueCountByCategoryName(final String categoryName) {
        try {
            return getInternalIssuesByCategoryName(categoryName).size();
        } catch (Exception e) {
            logger.warn("{}", e.getMessage(), e);
            return 0;
        }
    }

    public String getInvalidByStatusCategoryName() {
        return JiraIssueSearchType.INVALID_STATE.title();
    }

    public String getInvalidByFixVersionCategoryName() {
        return JiraIssueSearchType.INVALID_FIX_VERSION.title();
    }

    public ImmutableSet<ReportJiraIssueModel> getExternalIssuesByCategoryName(final String categoryName) {
        if (externalIssuesByCategory.containsKey(categoryName)) {
            return externalIssuesByCategory.get(categoryName);
        } else {
            return ImmutableSet.copyOf(new HashSet<ReportJiraIssueModel>());
        }
    }

    public ImmutableSet<ReportJiraIssueModel> getInternalIssuesByCategoryName(final String categoryName) {
        if (internalIssuesByCategory.containsKey(categoryName)) {
            return internalIssuesByCategory.get(categoryName);
        } else {
            return ImmutableSet.copyOf(new HashSet<ReportJiraIssueModel>());
        }
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

    public String getJqlLink() {
        return jqlLink; 
    }

    public Configuration getConfiguration() { 
        return configuration;
    }

    public ImmutableSet<String> getFixVersions() {
        return fixVersions;
    }

    public ImmutableSet<ReportJiraIssueModel> getKnownIssues() {
        return knownIssues;
    }

    public String getKnownIssuesJqlLink() {
        return knownIssuesJqlLink;
    }

    public Map<JiraIssueSearchType, String> getErrors() {
        return errors;
    }
    
    public String getKnownIssuesErrorMessage() {
        return errors.get(JiraIssueSearchType.KNOWN_ISSUE);
    }

    public String getFixVersionErrorMessage() {
        return errors.get(JiraIssueSearchType.FIX_VERSION);
    }

    public String getGenericErrorMessage() {
        return errors.get(JiraIssueSearchType.GENERIC);
    }


}
