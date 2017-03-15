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
</div>
</div>
</div>


<#list issueCategoryNames as categoryName>
<div class="row">
    <div class="col-md-8">
        <h3><p class="bg-success">Released issues with type ${categoryName} <span class="badge">${getIssuesByCategoryName(categoryName).size()}</span></p></h3>
    <ul>
        <#list getIssuesByCategoryName(categoryName) as issue>
            <li>
                <#list issue.defectIds as defect>
                    <span class="label label-danger">${defect}</span>
                </#list>

               <img alt="" src="https://ensemble.atlassian.net/images/icons/priorities/${issue.issue.priority.name?lower_case}.svg" title="Highest - This problem will block progress." height="16" width="16">
               </img>
               <a href="${issue.url}">${issue.issue.key}: ${issue.issue.summary} <span class="label label-warning">${(issue.fixedInFlowWebVersion! "")}</span> <span class="label label-warning">${(issue.fixVersions! "")}</span></a>
              <#if (issue.releaseNotes)??>
                 <ul><li><em>${issue.releaseNotes}</em></li></ul>
              </#if>
            </li>
        </#list>
    </ul>
  </div>
</div>
</#list>

<div class="row">
    <div class="col-md-8">
<h3><p class="bg-success">Commits with Defects <span class="badge">${commitsWithDefectIds.size()}</span></p></h3>
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
</div>
</div>

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

<div class="row">
    <div class="col-md-8">
<h3><p class="bg-success">Link to JIRA</p></h3>
<a href="${jqlLink}">Link to JIRA</a>
</div>
</div>

</body>
</html>
</#escape>
