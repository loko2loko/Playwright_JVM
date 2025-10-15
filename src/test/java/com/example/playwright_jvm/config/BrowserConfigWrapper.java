package com.example.playwright_jvm.config;

import java.util.Map;

public class BrowserConfigWrapper {
    private Map<String, BrowserConfigData> browsers;

    public Map<String, BrowserConfigData> getBrowsers() { return browsers; }
    public void setBrowsers(Map<String, BrowserConfigData> browsers) { this.browsers = browsers; }
}