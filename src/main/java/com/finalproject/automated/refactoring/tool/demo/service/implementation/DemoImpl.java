package com.finalproject.automated.refactoring.tool.demo.service.implementation;

import com.finalproject.automated.refactoring.tool.demo.service.Demo;
import com.finalproject.automated.refactoring.tool.files.detection.model.FileModel;
import com.finalproject.automated.refactoring.tool.files.detection.service.FilesDetection;
import com.finalproject.automated.refactoring.tool.longg.methods.detection.service.LongMethodsDetection;
import com.finalproject.automated.refactoring.tool.longg.parameter.methods.detection.service.LongParameterMethodsDetection;
import com.finalproject.automated.refactoring.tool.methods.detection.service.MethodsDetection;
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
    private FilesDetection filesDetection;

    @Autowired
    private MethodsDetection methodsDetection;

    @Autowired
    private LongMethodsDetection longMethodsDetection;

    @Autowired
    private LongParameterMethodsDetection longParameterMethodsDetection;

    private static final Long LONG_METHOD_THRESHOLD = 10L;
    private static final Long LONG_PARAMETER_METHOD_THRESHOLD = 3L;

    private static final Integer FIRST_INDEX = 0;
    private static final Integer ONE = 1;

    private static final String MIME_TYPE = "text/x-java-source";

    @Override
    public void doLongMethodsDetection(List<String> paths) {
        Map<String, List<FileModel>> files = filesDetection.detect(paths, MIME_TYPE);
        files.forEach(this::doReadFiles);
    }

    private void doReadFiles(String path, List<FileModel> fileModels) {
        System.out.println();
        System.out.println("Class for path -> " + path);
        System.out.println();

        Map<String, List<MethodModel>> methods = methodsDetection.detect(fileModels);
        methods.forEach(this::printMethod);

        doPrintMethodInformation(fileModels, methods);

        methods.forEach(this::doSearchCodeSmells);
    }

    private void doPrintMethodInformation(List<FileModel> fileModels, Map<String, List<MethodModel>> methods) {
        Long methodsCount = methods.values()
                .stream()
                .mapToLong(List::size)
                .sum();

        doPrintSeparator();

        System.out.println("Class size -> " + fileModels.size());
        System.out.println("Class has methods -> " + methods.size());
        System.out.println("Methods size -> " + methodsCount);
        System.out.println();

        doPrintSeparator();
    }

    private void printMethod(String methodPath, List<MethodModel> methodModels) {
        System.out.println("Methods for class -> " + methodPath);
        System.out.println();

        methodModels.forEach(this::doPrintMethod);
        System.out.println();
    }

    private void doSearchCodeSmells(String filename, List<MethodModel> methodModels) {
        doPrintSeparator();
        searchLongMethods(filename, methodModels);
        searchLongParameterMethods(filename, methodModels);
        doPrintSeparator();
    }

    private void searchLongMethods(String filename, List<MethodModel> methodModels) {
        System.out.println("Long method for filename --> " + filename);
        System.out.println();

        List<MethodModel> longMethods = longMethodsDetection.detect(methodModels, LONG_METHOD_THRESHOLD);

        if (!longMethods.isEmpty())
            longMethods.forEach(this::doPrintMethod);
        else
            System.out.println("Doesn't has long method code smell...");

        System.out.println();
    }

    private void searchLongParameterMethods(String filename, List<MethodModel> methodModels) {
        System.out.println("Long parameter method for filename --> " + filename);
        System.out.println();

        List<MethodModel> longParameterMethods = longParameterMethodsDetection.detect(
                methodModels, LONG_PARAMETER_METHOD_THRESHOLD);

        if (!longParameterMethods.isEmpty())
            longParameterMethods.forEach(this::doPrintMethod);
        else
            System.out.println("Doesn't has long parameter method code smell...");

        System.out.println();
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
        doPrintWithSpace("throws");

        Integer maxSize = methodModel.getExceptions().size() - ONE;

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

    private void doPrintWithSpace(String text) {
        System.out.print(text + " ");
    }

    private void doPrintSeparator() {
        System.out.println("------------------------------------------------------------------------");
        System.out.println();
    }
}
