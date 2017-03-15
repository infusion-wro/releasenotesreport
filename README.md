# release notes generator

## rng is java program for creating release notes from scm history (only git supported for now)

## Steps of work

1. Building configuration either from cli parameters and/or from given properties configuration file
2. Getting git log messages limited by tag(s) or commit id(s)
3. Matching jira issue ids from git messages based on given pattern
4. Creating report in html
5. Pushing generated report to git remote repository

## Sample use

### Command line with configuration .properties file
Parameters for program can be defined in .properties file defined by cli parameter named _configurationFilePath_

	java -jar target/release-notes-generator-1.0-SNAPSHOT.jar -configurationFilePath ./configuration.properties -tag1 0.19.0.20
sample configuration.properties file can be found in src/test/resources/configuration.properties

### Command line with cli parameters
	java -jar target/release-notes-generator-1.0-SNAPSHOT.jar -tag1 0.19.0.20 -gitDirectory "C:/temp/project" -gitBranch develop -gitUrl "https://github.company.com" -gitUsername username -gitPassword password -jiraUrl "https://jira.company.com" -jiraUsername username -jiraPassword password -jiraIssuePattern "SYM-\\d+"

### Java use
Java level use can be best achieved through builder ```com.infusion.relnotesgen.MainInvoker```
Check ```com.infusion.relnotesgen.MainITTest``` for appropriate use case.

	new MainInvoker()
                .pushReleaseNotes(true)
                .gitDirectory(localGit.getAbsolutePath())
                .gitBranch("master")
				...
                .invoke();

## Parameters overview

| .properties params name   | cli params name				| description         | example value |
 -------------------------- | ----------------------------- | ------------------  | ------ |
| n/a    					| -configurationFilePath  		| path to properties file with configuration params | ./../configuration.properties |
| n/a	 					| -commitId1            		| commit id 1st marker, determines from which commit in scm history should be taken. If commitId2 is null then all newest commits will be taken till commitId1 | d7ee6e45a9458bf1e5f483b7286246455462be73 |
| n/a	 					| -commitId2 					| commit id 2nd marker, history in scm will be read from commitId1 till commitId2. | d7ee6e45a9458bf1e5f483b7286246455462be73 |
| n/a	 					| -tag1 						| tag 1 name - connected commit to given tag will serve as commitId1 parameter | 1.0.0 |
| n/a	 			   		| -tag2 						| tag 2 name - connected commit to given tag will serve as commitId2 parameter | 1.0.0 |
| n/a	 			     	| -pushReleaseNotes 			| boolean parameter, define should push to remote repository under 'releases/version_number.html' should be performed | |
| git.url         			| -gitUrl 						| URL to git repository | https://stash.infusion.com/scm/en/symphony.git |
| git.directory    			| -gitDirectory 				| Path under which git repository is held localy. If none exists it will be cloned under this location. Directory structure will be created if it doesn't exist | C:/temp/testsymphony |
| git.branch       			| -gitBranch 					| Branch name from where scm history will be read and release notes will be pushed | develop |
| git.username      		| -gitUsername 					| Git username | johnny |
| git.password     			| -gitPassword 					| Git password | passw0rd123 |
| git.committer.name      	| -gitCommitterName 			| Sometimes it's needed to define this to pass validation rules on push operation | 'Johnny Bravo' |
| git.committer.mail  		| -gitCommitterMail 			| Sometimes it's needed to define this to pass validation rules on push operation | johnny@hairs.com |
| git.commitmessage.validationommiter | -gitCommitMessageValidationOmmiter | Suffix that will be appended to commit message under which release notes are commited | '#skipvalidation' |
| jira.url      			| -jiraUrl 						| URL to jira | https://ensemble.atlassian.net |
| jira.username   			| -jiraUsername 				| Jira username | johnny  |
| jira.password   			| -jiraPassword 				| Jira password | passw0rd123  |
| jira.issuepattern   		| -jiraIssuePattern 			| Pattern from which jira issue's id will be search in scm commit messages | SYM-\d+ |
| issue.filterby.component	| -issueFilterByComponent 		| List of jira's component's name separated by ',' if defined only issues that has at least one of those component will be in release notes (exacly jira's component's name must contains ignore case given here component) | System 1,veryImportan,Something something |
| issue.filterby.type		| -issueFilterByType 			| List of jira's issue type's name separated by ',' if defined only issues that has at least one of those type will be in release notes (defined here type name must exacly (ignore case) match type name of issues in jira) | New Feature,Bug,Technical Task |
| issue.filterby.label		| -issueFilterByLabel 			| List of jira's labels separated by ',' if defined only issues that has at least one of those labels will be in release notes (exacly jira's label's name must contains ignore case given here label) | label1,label2,label3 |
| issue.filterby.status		| -issueFilterByStatus 			| List of jira's issue statuses name separated by ',' if defined only issues that has one of those status will be in release notes (defined here status name must exacly (ignore case) match status name of issues in jira) | Ready for QA,Released,Done |
| issue.sort.type			| -issueSortType 				| Defines sort order of issues by type - issues are provided in report template context as map where key is issue type and value is list of issues with that type, this map is sorted | New Feature,Bug |
| issue.sort.priority		| -issueSortPriority 			| In report template issues are provided as map where key is issue type and value is list of issue with that type, this parameter defines order in list of issues | Highest,High,Medium,Low,Lowest |
| report.directory			| -reportDirectory 				| Directory where release notes will be saved | C:/temp |
| report.template			| -reportTemplate 				| Path to external template for release notes. Freemarker is used as template engine. Variables provided in context: \$\{issues\} - map of issues where key is issue type and value list of issues; \$\{jiraUrl\} - url to jira; \$\{version\} - version for which release notes are generated | C:/releaseNotes/template.ftl |


## Known issues

#### Jira ssl certificate
Attlasian jira client used in rng requires to have valid certificate for jira address stored in jre key store.
Detailed instruction on how add new certificate to sore:
https://confluence.atlassian.com/display/STASHKB/SSLHandshakeException+-+unable+to+find+valid+certification+path+to+requested+target

#### Search by latest tags
When rng is run without commitId\* and tag\* parameters then release notes will be generated for last 2 tags.
It may happen that those 2 tags are connected to commits that are not available on branch which rng is using - in that case RuntimeException will be thrown with message:
_No commit were found for given commit ids commitId1, commitId2. Maybe branch is badly chosen._
