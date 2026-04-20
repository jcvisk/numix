package com.numix.web.controller.api;

import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class I18nApiController {

    private final String bundleBaseName;

    public I18nApiController(@Value("${spring.messages.basename:i18n/messages}") String messageBaseName) {
        this.bundleBaseName = resolveBundleBaseName(messageBaseName);
    }

    @GetMapping({"/api/i18n", "/api/i18n/{module}"})
    public Map<String, String> messages(
        @PathVariable(value = "module", required = false) String module,
        Locale locale
    ) {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseName, locale);
        Set<String> prefixes = resolvePrefixes(module);
        Map<String, String> response = new TreeMap<>();

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (startsWithAnyPrefix(key, prefixes)) {
                response.put(key, bundle.getString(key));
            }
        }

        return response;
    }

    private String resolveBundleBaseName(String configuredBaseName) {
        if (configuredBaseName == null || configuredBaseName.isBlank()) {
            return "i18n.messages";
        }
        String firstBaseName = configuredBaseName.split(",")[0].trim();
        return firstBaseName.replace('/', '.');
    }

    private Set<String> resolvePrefixes(String module) {
        Set<String> prefixes = new LinkedHashSet<>();
        prefixes.add("general.");

        if (module != null) {
            String normalizedModule = module.trim();
            if (!normalizedModule.isEmpty()) {
                prefixes.add(normalizedModule + ".");
            }
        }

        return prefixes;
    }

    private boolean startsWithAnyPrefix(String key, Set<String> prefixes) {
        for (String prefix : prefixes) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
