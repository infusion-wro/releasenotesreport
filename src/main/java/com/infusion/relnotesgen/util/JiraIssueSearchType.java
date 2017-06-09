package com.infusion.relnotesgen.util;

public enum JiraIssueSearchType {
	GENERIC ("Generic", true), 
	INVALID_FIX_VERSION ("Invalid Fix Version(s)", false), 
    INVALID_STATE ("Invalid State", false), 
	FIX_VERSION ("FixVersion", true), 
	KNOWN_ISSUE ("KnownIssue", true);
    
    private String title;
    private boolean isValid;
    
    JiraIssueSearchType(String title, boolean isValid) {
        this.title = title;
        this.isValid = isValid;
    }
    
    public String title() {
        return title;
    }
    
    public boolean isValid() {
        return isValid;
    }
}
