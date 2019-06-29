package com.finalproject.automated.refactoring.tool.demo.service.implementation;

import com.finalproject.automated.refactoring.tool.demo.model.RemoveModelVA;
import com.finalproject.automated.refactoring.tool.demo.service.AutomatedRefactoring;
import com.finalproject.automated.refactoring.tool.detection.service.Detection;
import com.finalproject.automated.refactoring.tool.model.CodeSmellName;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.refactoring.service.Refactoring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 25 October 2018
 */

@Service
public class AutomatedRefactoringImpl implements AutomatedRefactoring {

    @Autowired
    private Detection detection;

    @Autowired
    private Refactoring refactoring;

    private static final Map<String, Map<String, List<MethodModel>>> globalRefactoringResult =
            new ConcurrentHashMap<>();

    private static final Integer FIRST_INDEX = 0;
    private static final Integer SECOND_INDEX = 1;

    @Override
    public void automatedRefactoring(List<String> paths) {
        Map<String, Map<String, List<MethodModel>>> detectResult = detection.detect(paths);
        Map<String, Map<String, List<MethodModel>>> resultChecked = checkRefactoringResults(detectResult);

        while (isHasSmells(resultChecked)) {
            refactoring(detectResult, resultChecked);

            detectResult = detection.detect(paths);
            resultChecked = checkRefactoringResults(detectResult);
        }
    }

    private Map<String, Map<String, List<MethodModel>>> checkRefactoringResults(
            Map<String, Map<String, List<MethodModel>>> detectResult) {
        return detectResult.entrySet()
                .parallelStream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entryResult -> getCodeSmellMethods(entryResult.getValue())));
    }

    private Map<String, List<MethodModel>> getCodeSmellMethods(Map<String, List<MethodModel>> result) {
        Map<String, List<MethodModel>> filteredResult = result.entrySet()
                .parallelStream()
                .peek(this::checkNewModel)
                .collect(Collectors.toMap(Map.Entry::getKey, this::filterMethodsByCodeSmell));

        return filteredResult.entrySet()
                .parallelStream()
                .filter(this::isValueNotEmpty)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void checkNewModel(Map.Entry<String, List<MethodModel>> resultEntry) {
        resultEntry.getValue()
                .forEach(this::removeOtherSmells);
        resultEntry.getValue()
                .forEach(methodModel -> removeOldModel(resultEntry.getKey(), methodModel));
    }

    private void removeOldModel(String path, MethodModel methodModel) {
        List<CodeSmellName> methodSmells = new ArrayList<>(methodModel.getCodeSmells());
        methodSmells.forEach(codeSmellName ->
                removeOldModelByCodeSmell(path, codeSmellName.getName(), methodModel));
    }

    private void removeOldModelByCodeSmell(String path, String codeSmell,
                                           MethodModel methodModel) {
        RemoveModelVA removeModelVA = RemoveModelVA.builder()
                .path(path)
                .codeSmell(codeSmell)
                .methodModel(methodModel)
                .build();

        globalRefactoringResult.computeIfPresent(codeSmell,
                (key, result) -> removeOldModelByPath(removeModelVA, result));
    }

    private Map<String, List<MethodModel>> removeOldModelByPath(RemoveModelVA removeModelVA,
                                                                Map<String, List<MethodModel>> result) {
        result.computeIfPresent(removeModelVA.getPath(),
                (key, methodModels) -> removeOldModel(removeModelVA, methodModels));
        return result;
    }

    private List<MethodModel> removeOldModel(RemoveModelVA removeModelVA,
                                             List<MethodModel> methodModels) {
        removeStatements(removeModelVA.getMethodModel());

        if (methodModels.contains(removeModelVA.getMethodModel())) {
            removeModelVA.getMethodModel()
                    .getCodeSmells()
                    .removeIf(codeSmellName ->
                            isOldModel(codeSmellName.getName(), removeModelVA.getCodeSmell()));
        }

        return methodModels;
    }

    private Boolean isOldModel(String codeSmell, String codeSmellExpected) {
        return codeSmell.equals(codeSmellExpected);
    }

    private void removeOtherSmells(MethodModel methodModel) {
        methodModel.getCodeSmells()
                .removeIf(this::notLongMethod);
    }

    private Boolean notLongMethod(CodeSmellName codeSmellName) {
        return !codeSmellName.equals(CodeSmellName.LONG_METHOD);
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

    private void refactoring(Map<String, Map<String, List<MethodModel>>> detectResult,
                             Map<String, Map<String, List<MethodModel>>> resultChecked) {
        for (Map.Entry<String, Map<String, List<MethodModel>>> entryResult : detectResult.entrySet()) {
            doRefactoring(entryResult, resultChecked.get(entryResult.getKey()));
        }
    }

    private void doRefactoring(Map.Entry<String, Map<String, List<MethodModel>>> entryResult,
                               Map<String, List<MethodModel>> codeSmellMethods) {
        printResultInfo(entryResult.getKey(), entryResult.getValue(), codeSmellMethods);
        printRefactoringLoadingText();

        Map<String, Map<String, List<MethodModel>>> result = refactoring.refactoring(
                codeSmellMethods);
        analysisRefactoringResult(result);
        printRefactoringReport(codeSmellMethods);
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

    private void printRefactoringLoadingText() {
        System.out.println("Do refactoring...");
        System.out.println();
    }

    private void analysisRefactoringResult(Map<String, Map<String, List<MethodModel>>> refactoringResult) {
        if (!refactoringResult.isEmpty()) {
            analysisFailedRefactoringResult(refactoringResult);
        }
    }

    private void analysisFailedRefactoringResult(Map<String, Map<String, List<MethodModel>>> result) {
        System.out.println("Failed Refactoring : ");
        System.out.println();

        result.forEach(this::saveFailedRefactoringResultBySmell);
        result.forEach(this::printFailedRefactoringResult);
    }

    private void printFailedRefactoringResult(String codeSmell, Map<String, List<MethodModel>> result) {
        System.out.println(codeSmell + " : ");
        result.forEach(this::printFailedResult);
    }

    private void printFailedResult(String path, List<MethodModel> methods) {
        int maxIndex = methods.size() - SECOND_INDEX;

        System.out.print("\t--> " + path + " : ");

        for (int index = FIRST_INDEX; index < methods.size(); index++) {
            doPrintFailedResult(index, maxIndex, methods);
        }

        System.out.println();
    }

    private void doPrintFailedResult(Integer index, Integer maxIndex, List<MethodModel> methods) {
        System.out.print(methods.get(index).getName());

        if (!index.equals(maxIndex)) {
            System.out.print(", ");
        } else {
            System.out.println();
        }
    }

    private void saveFailedRefactoringResultBySmell(String codeSmell,
                                                    Map<String, List<MethodModel>> result) {
        boolean isContainsCodeSmell = globalRefactoringResult.containsKey(codeSmell);

        if (isContainsCodeSmell) {
            Map<String, List<MethodModel>> savedResult = globalRefactoringResult.get(codeSmell);
            result.forEach((path, methodModels) ->
                    saveFailedRefactoringResultByPath(path, methodModels, savedResult));
        } else {
            saveNewFailedRefactoringResult(codeSmell, result);
        }
    }

    private void saveFailedRefactoringResultByPath(String path, List<MethodModel> result,
                                                   Map<String, List<MethodModel>> savedResult) {
        boolean containsKey = savedResult.containsKey(path);

        if (containsKey) {
            List<MethodModel> savedMethods = savedResult.get(path);
            result.forEach(methodModel -> saveFailedRefactoringResultByMethodModel(methodModel, savedMethods));
        } else {
            saveNewFailedRefactoringResultByPath(path, result, savedResult);
        }
    }

    private void saveFailedRefactoringResultByMethodModel(MethodModel methodModel,
                                                          List<MethodModel> savedMethods) {
        removeStatements(methodModel);

        if (!savedMethods.contains(methodModel)) {
            savedMethods.add(methodModel);
        }
    }

    private void saveNewFailedRefactoringResultByPath(String path, List<MethodModel> result,
                                                      Map<String, List<MethodModel>> savedResult) {
        result.forEach(this::removeStatements);
        savedResult.put(path, result);
    }

    private void removeStatements(MethodModel methodModel) {
        methodModel.setStatements(new ArrayList<>());
    }

    private void saveNewFailedRefactoringResult(String codeSmell,
                                                Map<String, List<MethodModel>> result) {
        result.values()
                .parallelStream()
                .flatMap(Collection::parallelStream)
                .forEach(this::removeStatements);

        globalRefactoringResult.put(codeSmell, result);
    }

    private Boolean isHasSmells(Map<String, Map<String, List<MethodModel>>> result) {
        return result.values()
                .parallelStream()
                .map(Map::values)
                .flatMap(Collection::parallelStream)
                .flatMap(Collection::parallelStream)
                .anyMatch(this::hasCodeSmells);
    }

    private void printRefactoringReport(Map<String, List<MethodModel>> codeSmellMethods) {
        System.out.println("Methods smells refactored --> " + codeSmellMethods.values()
                .parallelStream()
                .flatMap(Collection::parallelStream)
                .filter(methodModel -> methodModel.getCodeSmells().isEmpty())
                .count());
        System.out.println();
        System.out.println("Refactoring complete...");
    }
}
