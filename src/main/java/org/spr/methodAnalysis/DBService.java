package org.spr.methodAnalysis;

import java.util.List;

public interface DBService {
    boolean addData(Object data) throws Exception;

    boolean addData(List<? extends Object> data) throws Exception;

    List<String> getAllInvokedMethods(String className, String methodName, String methodParameters);
}