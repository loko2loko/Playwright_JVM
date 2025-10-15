package com.example.playwright_jvm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BrowserConfigLoader {

    public static Map<String, BrowserConfig> loadConfigs() {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InputStream input = BrowserConfigLoader.class.getResourceAsStream("/browsers.yml");

            if (input == null) {
                throw new RuntimeException("browsers.yml not found in resources");
            }

            // Parsuj YAML do wrapper objektu
            BrowserConfigWrapper wrapper = mapper.readValue(input, BrowserConfigWrapper.class);

            // Konvertuj na BrowserConfig objekty
            Map<String, BrowserConfig> configs = new HashMap<>();
            wrapper.getBrowsers().forEach((name, data) -> {
                configs.put(name, new BrowserConfig(
                        data.getWidth(),
                        data.getHeight(),
                        data.isMobile(),
                        data.getDisplayName()
                ));
            });

            return configs;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load browser configuration from browsers.yml: " + e.getMessage(), e);
        }
    }
}