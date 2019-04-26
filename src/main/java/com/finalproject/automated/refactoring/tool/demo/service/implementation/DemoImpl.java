package com.finalproject.automated.refactoring.tool.demo.service.implementation;

import com.finalproject.automated.refactoring.tool.demo.service.Demo;
import com.finalproject.automated.refactoring.tool.detection.service.Detection;
import com.finalproject.automated.refactoring.tool.model.CodeSmellName;
import com.finalproject.automated.refactoring.tool.model.MethodModel;
import com.finalproject.automated.refactoring.tool.model.PropertyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 25 October 2018
 */

@Service
public class DemoImpl implements Demo {

    @Autowired
    private Detection detection;

    private static final Integer FIRST_INDEX = 0;
    private static final Integer ONE = 1;

    @Override
    public void doCodeSmellsDetection(List<String> paths) {
        Map<String, Map<String, List<MethodModel>>> result = detection.detect(paths);
        result.forEach(this::doReadResults);
    }

    private void doReadResults(String path, Map<String, List<MethodModel>> result) {
        System.out.println();
        System.out.println("Class for path -> " + path);
        System.out.println();
        System.out.println("Class size -> " + result.size());
        System.out.println("Class has methods -> " + getClassHasMethodsCount(result));
        System.out.println("Methods size -> " + getMethodsCount(result));

        result.forEach(this::doReadResult);
    }

    private Long getClassHasMethodsCount(Map<String, List<MethodModel>> result) {
        return result.values()
                .parallelStream()
                .filter(this::hasMethods)
                .count();
    }

    private Integer getMethodsCount(Map<String, List<MethodModel>> result) {
        return result.values()
                .parallelStream()
                .mapToInt(List::size)
                .sum();
    }

    private Boolean hasMethods(List<MethodModel> methods) {
        return methods.size() >= ONE;
    }

    private void doReadResult(String path, List<MethodModel> methods) {
        System.out.println();
        System.out.println("Class --> " + path);
        System.out.println();

        printMethod(methods);
    }

    private void printMethod(List<MethodModel> methods) {
        if (methods.size() >= ONE)
            methods.forEach(this::doPrintMethod);
        else
            System.out.println("Method --> Didn't has methods");
    }

    private void doPrintMethod(MethodModel methodModel) {
        doPrintWithSpace("Method -->");
        doPrintMethodKeywords(methodModel);
        doPrintMethodReturnType(methodModel);

        System.out.print(methodModel.getName());
        System.out.print("(");

        doPrintMethodParameters(methodModel);
        doPrintWithSpace(")");

        doPrintMethodExceptions(methodModel);
        doPrintMethodLOC(methodModel);
        doPrintMethodCodeSmells(methodModel);

        System.out.println();
    }

    private void doPrintMethodKeywords(MethodModel methodModel) {
        methodModel.getKeywords()
                .forEach(this::doPrintWithSpace);
    }

    private void doPrintMethodReturnType(MethodModel methodModel) {
        if (isHasReturnType(methodModel))
            doPrintWithSpace(methodModel.getReturnType());
    }

    private Boolean isHasReturnType(MethodModel methodModel) {
        Optional<String> returnType = Optional.ofNullable(methodModel.getReturnType());
        return returnType.isPresent() && !returnType.get().isEmpty();
    }

    private void doPrintMethodParameters(MethodModel methodModel) {
        Integer maxSize = methodModel.getParameters().size() - ONE;

        for (Integer index = FIRST_INDEX; index < methodModel.getParameters().size(); index++)
            doPrintMethodParameter(methodModel.getParameters().get(index), index, maxSize);
    }

    private void doPrintMethodParameter(PropertyModel propertyModel, Integer index, Integer maxSize) {
        System.out.print(propertyModel.getType() + " " + propertyModel.getName());
        doPrintCommaSeparator(index, maxSize);
    }

    private void doPrintMethodExceptions(MethodModel methodModel) {
        Integer maxSize = methodModel.getExceptions().size() - ONE;

        if (!methodModel.getExceptions().isEmpty())
            doPrintWithSpace("throws");

        for (Integer index = FIRST_INDEX; index < methodModel.getExceptions().size(); index++)
            doPrintMethodException(methodModel.getExceptions().get(index), index, maxSize);
    }

    private void doPrintMethodException(String exception, Integer index, Integer maxSize) {
        System.out.print(exception);
        doPrintCommaSeparator(index, maxSize);
    }

    private void doPrintCommaSeparator(Integer index, Integer maxSize) {
        if (!index.equals(maxSize))
            doPrintWithSpace(",");
    }

    private void doPrintMethodLOC(MethodModel methodModel) {
        Optional<Long> loc = Optional.ofNullable(methodModel.getLoc());

        if (loc.isPresent())
            System.out.print(" --> LOC : " + methodModel.getLoc());
    }

    private void doPrintMethodCodeSmells(MethodModel methodModel) {
        Integer maxSize = methodModel.getCodeSmells().size() - ONE;

        if (!methodModel.getCodeSmells().isEmpty())
            doPrintWithSpace(" --> Smells :");

        for (Integer index = FIRST_INDEX; index < methodModel.getCodeSmells().size(); index++)
            doPrintMethodCodeSmell(methodModel.getCodeSmells().get(index), index, maxSize);
    }

    private void doPrintMethodCodeSmell(CodeSmellName codeSmellName, Integer index, Integer maxSize) {
        System.out.print(codeSmellName);
        doPrintCommaSeparator(index, maxSize);
    }

    private void doPrintWithSpace(String text) {
        System.out.print(text + " ");
    }

    private void doPrintSeparator() {
        System.out.println("------------------------------------------------------------------------");
        System.out.println();
    }
}
