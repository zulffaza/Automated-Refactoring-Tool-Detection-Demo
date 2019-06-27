package com.finalproject.automated.refactoring.tool.demo.service.implementation;

import com.finalproject.automated.refactoring.tool.demo.service.Demo;
import com.finalproject.automated.refactoring.tool.detection.service.Detection;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.refactoring.service.Refactoring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 25 October 2018
 */

@Service
public class DemoImpl implements Demo {

    @Autowired
    private Detection detection;

    @Autowired
    private Refactoring refactoring;

    private static final Map<String, Map<String, List<MethodModel>>> globalRefactoringResult = new HashMap<>();

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;

    @Override
    public void doCodeSmellsDetection(List<String> paths) {
        Map<String, Map<String, List<MethodModel>>> result = detection.detect(paths);
        result.forEach(this::doReadResults);
    }

    private void doReadResults(String path, Map<String, List<MethodModel>> result) {
        Map<String, List<MethodModel>> codeSmellMethods = getCodeSmellMethods(result);
        printResultInfo(path, result, codeSmellMethods);

        System.out.println("Do refactoring...");
        System.out.println();

        Map<String, Map<String, List<MethodModel>>> refactoringResult = refactoring.refactoring(
                codeSmellMethods);

        if (!refactoringResult.isEmpty()) {
            System.out.println("Failed Refactoring : ");
            System.out.println();

            refactoringResult.forEach(this::printFailedRefactoringResult);
        }

        System.out.println();
        System.out.println("Methods smells gone --> " + codeSmellMethods.values()
                .parallelStream()
                .flatMap(Collection::parallelStream)
                .filter(methodModel -> methodModel.getCodeSmells().isEmpty())
                .count());
    }

    private void printResultInfo(String path, Map<String, List<MethodModel>> result,
                                 Map<String, List<MethodModel>> codeSmellMethods) {
        System.out.println();
        System.out.println("Class for path -> " + path);
        System.out.println();
        System.out.println("Class size -> " + result.size());
        System.out.println("Class has methods -> " + getClassHasMethodsCount(result));
        System.out.println("Methods size -> " + getMethodsCount(result));
        System.out.println("Methods has smells -> " + getMethodsCount(codeSmellMethods));
        System.out.println();
    }

    private Map<String, List<MethodModel>> getCodeSmellMethods(Map<String, List<MethodModel>> result) {
        Map<String, List<MethodModel>> filteredResult = result.entrySet()
                .parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey, this::filterMethodsByCodeSmell));

        return filteredResult.entrySet()
                .parallelStream()
                .filter(this::isValueNotEmpty)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<MethodModel> filterMethodsByCodeSmell(Map.Entry<String, List<MethodModel>> resultEntry) {
        return resultEntry.getValue()
                .parallelStream()
                .filter(this::hasCodeSmells)
                .collect(Collectors.toList());
    }

    private Boolean hasCodeSmells(MethodModel methodModel) {
        return !methodModel.getCodeSmells().isEmpty();
    }

    private Boolean isValueNotEmpty(Map.Entry<String, List<MethodModel>> resultEntry) {
        return !resultEntry.getValue().isEmpty();
    }

    private Long getClassHasMethodsCount(Map<String, List<MethodModel>> result) {
        return result.values()
                .parallelStream()
                .filter(this::hasMethods)
                .count();
    }

    private Boolean hasMethods(List<MethodModel> methods) {
        return !methods.isEmpty();
    }

    private Integer getMethodsCount(Map<String, List<MethodModel>> result) {
        return result.values()
                .parallelStream()
                .mapToInt(List::size)
                .sum();
    }

    private void printFailedRefactoringResult(String codeSmell, Map<String, List<MethodModel>> result) {
        System.out.println(codeSmell + " : ");
        result.forEach(this::printFailedResult);
    }

    private void printFailedResult(String path, List<MethodModel> methods) {
        int maxIndex = methods.size() - SECOND_INDEX;

        System.out.print("--> " + path + " : ");

        for (int index = FIRST_INDEX; index < methods.size(); index++) {
            System.out.print(methods.get(index).getName());

            if (index != maxIndex) {
                System.out.print(", ");
            }
        }

        System.out.println();
    }
}
