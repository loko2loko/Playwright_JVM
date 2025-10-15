package com.example.playwright_jvm.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BrowserConfigData {
    private int width;
    private int height;
    private boolean mobile;
    @JsonProperty("displayName")
    private String displayName;

    // Default constructor pre Jackson
    public BrowserConfigData() {}

    // Getters a setters
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public boolean isMobile() { return mobile; }
    public void setMobile(boolean mobile) { this.mobile = mobile; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}