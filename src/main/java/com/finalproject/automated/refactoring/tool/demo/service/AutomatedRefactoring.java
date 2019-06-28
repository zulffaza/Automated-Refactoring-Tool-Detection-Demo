package com.finalproject.automated.refactoring.tool.demo.service;

import org.springframework.lang.NonNull;

import java.util.List;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 25 October 2018
 */

public interface AutomatedRefactoring {

    void automatedRefactoring(@NonNull List<String> paths);
}
