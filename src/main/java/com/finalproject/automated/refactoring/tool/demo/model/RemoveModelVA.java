package com.finalproject.automated.refactoring.tool.demo.model;

import com.finalproject.automated.refactoring.tool.model.MethodModel;
import lombok.Builder;
import lombok.Data;

/**
 * @author Faza Zulfika P P
 * @version 1.0.0
 * @since 28 June 2019
 */

@Data
@Builder
public class RemoveModelVA {

    private String path;

    private String codeSmell;

    private MethodModel methodModel;
}
