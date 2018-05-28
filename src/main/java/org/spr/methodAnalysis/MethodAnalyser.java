package org.spr.methodAnalysis;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MethodAnalyser {
    private DBInteractable database;
    private static final Logger LOGGER = Logger.getLogger(MethodAnalyser.class.getName());
    public MethodAnalyser(DBInteractable database) {
        this.database = database;
    }

    public List<String> traceMethodCalls(String className, String methodName, String methodParameters) {
        return traceMethodCalls(className, methodName, methodParameters, "");
    }

    private List<String> traceMethodCalls(String className, String methodName, String methodParameters, String depth) {
        if (className.endsWith(".class"))
            className = className.split("\\.")[0];

        List<String> allInvokedMethods = new ArrayList<String>();
        allInvokedMethods.add(depth + className + " " + methodName);

        LOGGER.info(depth + className + " " + methodName);

        List<String> data = database.getAllInvokedMethods(className, methodName, methodParameters);

        Iterator<String> iterator = data.iterator();

        while (iterator.hasNext()) {
            String invokedMethod = iterator.next();
            String methodDetails[] = invokedMethod.split(" ");
            String invokedMethodClassName = methodDetails[0];
            String invokedMethodName = methodDetails[1];
            String invokedMethodParameters = methodDetails[2];
            allInvokedMethods.addAll(traceMethodCalls(invokedMethodClassName, invokedMethodName, invokedMethodParameters, depth + "\t"));
        }
        return allInvokedMethods;
    }
}
