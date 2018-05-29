package org.spr.methodAnalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MethodAnalyser {
    private DBService database;

    public MethodAnalyser(DBService database) {
        this.database = database;
    }

    /**
     * Get sequence of all methods call at runtime.
     * @param className
     * @param methodName
     * @param methodParameters
     * @return
     */
    public List<String> traceMethodCalls(String className, String methodName, String methodParameters) {
        return traceMethodCalls(className, methodName, methodParameters, "");
    }

    /**
     * Get sequence of all methods call at runtime with depth.
     * @param className
     * @param methodName
     * @param methodParameters
     * @param depth spacing denoting depth of call
     * @return
     */
    private List<String> traceMethodCalls(String className, String methodName, String methodParameters, String depth) {
        if (className.endsWith(".class"))
            className = className.split("\\.")[0];

        List<String> allInvokedMethods = new ArrayList<String>();
        allInvokedMethods.add(depth + className + " " + methodName + " " + methodParameters);

        System.out.println(depth + className + " " + methodName + " " + methodParameters);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
