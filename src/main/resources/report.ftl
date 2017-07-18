<#macro displayInvalidIssuesForCategory categoryName>
    <h4><p class="bg-success">${categoryName} <span class="badge">${getTotalIssueCountByCategoryName(categoryName)}</span></p></h3>
    <ul>
        <#list getExternalIssuesByCategoryName(categoryName) as issue>
			<@showIssueDetails issue=issue/>
        </#list>
    </ul>
    <#if ((getInternalIssueCountByCategoryName(categoryName)) gt 0)>
	    <div style="margin:10px 40px">
		    <h4><p class="bg-success"><span class="badge">INTERNAL</span> ${categoryName} <span class="badge">${getInternalIssueCountByCategoryName(categoryName)}</span></p></h3>
		    <ul>
		        <#list getInternalIssuesByCategoryName(categoryName) as issue>
					<@showIssueDetails issue=issue/>
		        </#list>
		    </ul>
	    </div>
    </#if>
</#macro>

<#macro displayInvalidIssuesForCategoryByStatus categoryName>
	<h4><p class="bg-success">${categoryName} <span class="badge">${getTotalIssueCountByCategoryName(categoryName)}</span></p></h3>

    <ul>
        <#list getExternalInvalidStateIssueStatuses() as status>
 	    	<h4><p class="bg-success">${status}</p></h3>
 	       	<#list getExternalInvalidStateIssuesByStatus(status) as issue>
 				<@showIssueDetails issue=issue/>
 	       </#list>
        </#list>
    </ul>
    <#if ((getInternalIssueCountByCategoryName(categoryName)) gt 0)>
	    <div style="margin:10px 40px">
		    <h4><p class="bg-success"><span class="badge">INTERNAL</span> ${categoryName} <span class="badge">${getInternalIssueCountByCategoryName(categoryName)}</span></p></h3>
		    <ul>
        		<#list getInternalInvalidStateIssueStatuses() as status>
    			<h4><p class="bg-success">${status}</p></h3>
    		   		<#list getInternalInvalidStateIssuesByStatus(status) as issue>
 						<@showIssueDetails issue=issue/>
 	    	   		</#list>
        		</#list>
    		</ul>
	    </div>
    </#if>
</#macro>

<#macro displayAllValidIssues>
    <#list getIssueCategoryNamesList() as categoryName>
        <div class="row">
            <div class="col-md-8">
                <#if (!getGenericErrorMessage().isEmpty())>
	                <p>Error: ${getGenericErrorMessage()}</p>
                </#if>
              	<@displayValidIssue categoryName=categoryName/>
            </div>
        </div>
    </#list>
</#macro>

<#macro displayValidIssue categoryName>
    <#if ((getTotalIssueCountByCategoryName(categoryName)) gt 0)>
        <#if (!categoryNameIsInvalid(categoryName))>
            <h3><p class="bg-success">Released issues with type ${categoryName} <span class="badge">${getTotalIssueCountByCategoryName(categoryName)}</span></p></h3>
            <ul>
                <#list getExternalIssuesByCategoryName(categoryName) as issue>
					<@showIssueDetails issue=issue/>
                </#list>
            </ul>
		    <#if ((getInternalIssueCountByCategoryName(categoryName)) gt 0)>
			    <div style="margin:10px 40px">
		            <h3><p class="bg-success"><span class="badge">INTERNAL</span> Released issues with type ${categoryName} <span class="badge">${getInternalIssueCountByCategoryName(categoryName)}</span></p></h3>
		            <ul>
		                <#list getInternalIssuesByCategoryName(categoryName) as issue>
							<@showIssueDetails issue=issue/>
		                </#list>
		            </ul>
		    	</div>
		    </#if>
        </#if>
    </#if>
</#macro>

<#macro displayInvalidIssues>
    <h3><p class="bg-success">Invalid Issues <span class="badge">${getTotalInvalidIssueCount()}</span></p></h3>
    <div style="margin:10px 40px">
		<@commitsWithDefectsSection commitsWithDefectIds=commitsWithDefectIds/>
		<@displayInvalidIssuesForCategoryByStatus categoryName=getInvalidByStatusCategoryName()/>
		<@displayInvalidIssuesForCategory categoryName=getInvalidByFixVersionCategoryName()/>
    </div>
</#macro>

<#macro commitsWithDefectsSection commitsWithDefectIds>
    <h4><p class="bg-success">Commits with Defects <span class="badge">${commitsWithDefectIds.size()}</span></p></h4>
    <ul>
        <#list commitsWithDefectIds as commit>
            <li>
                <#if commit.defectIds?has_content>
                    <#list commit.defectIds as defectId>
                        <span class="label label-danger">${defectId}</span>
                    </#list>
                </#if>
[id: ${commit.id}] ${commit.author} ${commit.message}
            </li>
        </#list>
    </ul>
</#macro>

<#macro defectListSection uniqueDefects>
    <div class="row">
        <div class="col-md-8">
            <h3><p class="bg-success">All defects </p></h3>
            <ol>
                <#list uniqueDefects as defect>
                    <li>
                        <span class="label label-danger">${defect}</span>
                    </li>
                </#list>
            </ol>
        </div>
    </div>
</#macro>

<#macro linkToJiraSection jqlLink>
    <div class="row">
        <div class="col-md-8">
            <h3><p class="bg-success">Link to JIRA</p></h3>
            <a href="${jqlLink}">Link to JIRA</a>
            <p/>
        </div>
    </div>
</#macro>

<#macro knownIssuesSection knownIssues knownIssuesJqlLink>
    <div class="row">
        <div class="col-md-8">
            <h3><p class="bg-success">Known Issues <span class="badge">${knownIssues.size()}</span></p></h3>
            <#if (knownIssues.size() > 0)>
                <a href="${knownIssuesJqlLink}">Link to JIRA</a>
            </#if>
            <#if (!getKnownIssuesErrorMessage().isEmpty())>
                <p>Error: ${getKnownIssuesErrorMessage()}</p>
            </#if>
            <ul>
                <#list getKnownIssues() as issue>
                    <li>
                        <#list issue.defectIds as defect>
                            <span class="label label-danger">${defect}</span>
                        </#list>
                        <img alt="" src="https://ensemble.atlassian.net/images/icons/priorities/${issue.issue.priority.name?lower_case}.svg" title="${issue.issue.priority.name}" height="16" width="16">
                        </img>
                        <div>
                            <a href="${issue.url}">${issue.issue.key}: ${issue.issue.summary} </a>
                            <span class="label label-warning">${(issue.fixedInFlowWebVersion! "")}</span>
                            <#if (issue.isStatusOk)>
                                <span class="label label-success">${(issue.status! "")}</span>
                            <#else>
                                <span class="label label-danger">${(issue.status! "")}</span>
                            </#if>
                            <#list issue.pullRequestIds as prId>
                                <a href="${configuration.gitBrowsePrsUrl + prId}"><span class="label label-warning">PR:${prId}</span> </a>
                            </#list>
                            <span class="label label-warning">${(issue.fixVersions! "")}</span>
                        </div>
                        <#if (issue.releaseNotes)??>
                            <ul><li><b>Release Notes: </b><em>${issue.releaseNotes}</em></li></ul>
                        </#if>
                        <#if (issue.impact)??>
                            <ul><li><b>Impact: </b><em>${issue.impact}</em></li></ul>
                        </#if>
                        <#if (issue.detailsOfChange)??>
                            <ul><li><b>Details of change: </b><em>${issue.detailsOfChange}</em></li></ul>
                        </#if>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
</#macro>

<#macro showIssueDetails issue>
    <li>
        <div>
            <#list issue.defectIds as defect>
                <span class="label label-danger">${defect}</span>
            </#list>

            <img alt="" src="https://ensemble.atlassian.net/images/icons/priorities/${issue.issue.priority.name?lower_case}.svg" title="${issue.issue.priority.name}" height="16" width="16">
            </img>

            <a href="${issue.url}">${issue.issue.key}: ${issue.issue.summary} </a>
            <span class="label label-warning">${(issue.fixedInFlowWebVersion! "")}</span>
            <#if (issue.isStatusOk)>
                <span class="label label-success">${(issue.status! "")}</span>
                <#else>
                    <span class="label label-danger">${(issue.status! "")}</span>
                </#if>
                <#list issue.pullRequestIds as prId>
                    <a href="${configuration.gitBrowsePrsUrl + prId}"><span class="label label-warning">PR:${prId}</span> </a>
                </#list>
                <span class="label label-warning">${(issue.fixVersions! "")}</span>
        </div>
        <#if (issue.releaseNotes)??>
            <ul><li><b>Release Notes: </b><em>${issue.releaseNotes}</em></li></ul>
        </#if>
        <#if (issue.impact)??>
            <ul><li><b>Impact: </b><em>${issue.impact}</em></li></ul>
        </#if>
        <#if (issue.detailsOfChange)??>
            <ul><li><b>Details of change: </b><em>${issue.detailsOfChange}</em></li></ul>
        </#if>
    </li>
</#macro>

<#escape x as x?html>
<html>
<head>
    <title>Release notes for version ${releaseVersion}</title>
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-10">
                <div class="page-header">
                    <h1>Release notes for version ${releaseVersion}</h1>
                    <h2><small>Generated for commits<span class="badge">${commitsCount}</span> from branch <strong>${gitBranch}</strong> between ${commitTag1.commit}<span class="label label-success">${(commitTag1.tag!"")}</span> and ${commitTag2.commit}<span class="label label-success">${(commitTag2.tag!"")}</span></small></h2>
	                <h2><small>
	                <#if (!fixVersions.isEmpty())>
		                <p>Fix versions: ${fixVersions}</p>
	                </#if>
	                <#if (!getFixVersionErrorMessage().isEmpty())>
		                <p>Error: ${getFixVersionErrorMessage()}</p>
	                </#if>
	                </h2></small>
                </div>
            </div>
        </div>

       	<@displayInvalidIssues/>

       	<@displayAllValidIssues/>

	<@defectListSection uniqueDefects=uniqueDefects/>

	<@linkToJiraSection jqlLink=jqlLink/>

	<@knownIssuesSection knownIssues=knownIssues knownIssuesJqlLink=knownIssuesJqlLink/>
</body>
</html>
</#escape>
