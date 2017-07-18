package com.infusion.relnotesgen.util;

public enum ModelViewLevel {
	INTERNAL ("Internal"), 
	EXTERNAL ("External");
    
    private String title;
    
    ModelViewLevel(String title) {
        this.title = title;
    }
    
    public String title() {
        return title;
    }
}
