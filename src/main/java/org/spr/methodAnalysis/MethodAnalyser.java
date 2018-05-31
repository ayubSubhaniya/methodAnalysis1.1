package org.spr.methodAnalysis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class MethodAnalyser {
    private DBService database;
    static final int MAX_LEVEL = 10;
    private final HashSet<String> methodCallStack = new HashSet<>();
    private final HashMap<String, JSONObject> allMethodsCall = new HashMap<>();

    private static final String filters[] = new String[]{
      "logger", "Logger", "logging", "Exception", "exception"
    };

    public MethodAnalyser(DBService database) {
        this.database = database;
    }

    /**
     * Get sequence of all methods call at runtime.
     *
     * @param className
     * @param methodName
     * @param methodParameters
     * @return
     */
    public JSONObject traceMethodCalls(String className, String methodName, String methodParameters) {
        return traceMethodCalls(className, methodName, methodParameters, 0);
    }

    /**
     * Get sequence of all methods call at runtime with depth.
     *
     * @param className
     * @param methodName
     * @param methodParameters
     * @param level
     * @return
     */
    private JSONObject traceMethodCalls(String className, String methodName, String methodParameters, int level) {

        if (methodCallStack.contains(className+" "+methodName+" "+methodParameters))
            return null;

        if (allMethodsCall.containsKey(className+" "+methodName+" "+methodParameters))
            return allMethodsCall.get(className+" "+methodName+" "+methodParameters);

        if (level>MAX_LEVEL /*|| methodName.compareTo("<init>")==0 /*|| methodName.startsWith("get") || methodName.startsWith("set")*/)
        {
            return null;
        }

        //for (String s:filters)
          //  if (className.contains(s) || methodName.contains(s))
            //    return Collections.emptyList();

        if (className.endsWith(".class"))
            className = className.split("\\.")[0];

        List<String> listOfInvokedMethods;

        if (database.isInterface(className)) {
            String interfaceName = className;
            String concreteClass = getConcreteClassOfInterface(interfaceName);
            if (concreteClass != null) {
                className = concreteClass;
            }
            else
                return null;
        }

        listOfInvokedMethods = database.getAllInvokedMethods(className, methodName, methodParameters);

        if (listOfInvokedMethods == null) {
            listOfInvokedMethods = getInvokedMethodsFromInheritedMethod(className, methodName, methodParameters);
        }

        if (listOfInvokedMethods != null) {
            if (methodCallStack.contains(className+" "+methodName+" "+methodParameters))
                return null;
            else
                methodCallStack.add(className+" "+methodName+" "+methodParameters);

            JSONObject currentMethods = new JSONObject();
            currentMethods.put(MethodCallTreeJSONFields.METHOD_SIGNATURE, getMethodSignature(className,methodName,methodParameters));

            Iterator<String> iterator = listOfInvokedMethods.iterator();
            JSONArray allInvokedMethods = new JSONArray();
            while (iterator.hasNext()) {
                String invokedMethod = iterator.next();
                String methodDetails[] = invokedMethod.split(" ");
                String invokedMethodClassName = methodDetails[0];
                String invokedMethodName = methodDetails[1];
                String invokedMethodParameters = methodDetails[2];
                JSONObject methodCallsOfInvokedMethod = traceMethodCalls(invokedMethodClassName, invokedMethodName, invokedMethodParameters, level+1);
                if (methodCallsOfInvokedMethod!=null)
                    allInvokedMethods.put(methodCallsOfInvokedMethod);
            }
            if (allInvokedMethods.length()>0)
                currentMethods.put(MethodCallTreeJSONFields.INVOKED_METHODS,allInvokedMethods);
            methodCallStack.remove(className+" "+methodName+" "+methodParameters);
            allMethodsCall.put(className+" "+methodName+" "+methodParameters,currentMethods);
            return currentMethods;
        }
        return null;
    }

    private String getConcreteClassOfInterface(String interfaceName) {
        List<String> implementedClasses = database.getImplementedClasses(interfaceName);
        if (implementedClasses.size() == 0) {
            List<String> interfacesThatExtendCurrent = database.getInterfacesThatExtendInterface(interfaceName);
            for (String childInterface : interfacesThatExtendCurrent) {
                String concreteClass = getConcreteClassOfInterface(childInterface);
                if (concreteClass != null)
                    return concreteClass;
            }
        } else
            return implementedClasses.get(0);

        return null;
    }

    /**
     * Method checks if any parent class has the given method
     *
     * @param className        String Name of class which contains the method to be found in parent class
     * @param methodName       String Name of method to be found in parent class
     * @param methodParameters String Method parameters of the method to be found in parent class
     * @return
     */
    private List<String> getInvokedMethodsFromInheritedMethod(String className, String methodName, String methodParameters) {
        String parentClassName = database.getSuperClassName(className);
        List<String> allInvokedMethodsOfInheritedMethod;
        while (parentClassName!=null) {
            allInvokedMethodsOfInheritedMethod = database.getAllInvokedMethods(parentClassName, methodName, methodParameters);
            if (allInvokedMethodsOfInheritedMethod != null)
                return allInvokedMethodsOfInheritedMethod;
            else
                parentClassName = database.getSuperClassName(parentClassName);
        }

        return null;
    }

    private String getMethodSignature(String classRelativePath, String methodName, String methodParameter){
        String classRelativePathSeparated[] = classRelativePath.split(File.separator);
        String className = classRelativePathSeparated[classRelativePathSeparated.length-1];
        String methodSignature = className+"@"+methodName+"    "+methodParameter;
        return methodSignature;
    }

}