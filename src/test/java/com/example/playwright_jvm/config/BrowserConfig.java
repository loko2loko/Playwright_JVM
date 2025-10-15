package com.example.playwright_jvm.config;

public class BrowserConfig {
    private final int width;
    private final int height;
    private final boolean mobile;
    private final String displayName;

    public BrowserConfig(int width, int height, boolean mobile, String displayName) {
        this.width = width;
        this.height = height;
        this.mobile = mobile;
        this.displayName = displayName;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isMobile() { return mobile; }
    public String getDisplayName() { return displayName; }
}