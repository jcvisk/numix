package com.numix.web.controller.view;

import java.util.Objects;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class BaseViewController {

    public static final String MODEL_ATTRIBUTE_MODULE = "MODULE";
    private final String module;

    protected BaseViewController(String module) {
        String normalizedModule = Objects.requireNonNull(module, "module is required").trim();
        if (normalizedModule.isEmpty()) {
            throw new IllegalArgumentException("module is required");
        }
        this.module = normalizedModule;
    }

    @ModelAttribute(MODEL_ATTRIBUTE_MODULE)
    public String moduleAttribute() {
        return module;
    }
}
