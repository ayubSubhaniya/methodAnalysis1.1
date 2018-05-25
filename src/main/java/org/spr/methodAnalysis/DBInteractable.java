package org.spr.methodAnalysis;

import java.io.IOException;
import java.util.List;

public interface DBInteractable {
    public boolean addData(Object data) throws Exception;

    List<String> getAllInvokedMethods(String className, String methodName,String methodParameters);
}