package org.spr.methodAnalysis;

import java.util.List;

public interface DBInteractable {
    boolean addData(Object data) throws Exception;

    List<String> getAllInvokedMethods(String className, String methodName, String methodParameters);
}