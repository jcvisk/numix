package com.numix.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource(value = "classpath:numix-core.properties", ignoreResourceNotFound = true)
public class CoreSharedPropertiesAutoConfiguration {
}
