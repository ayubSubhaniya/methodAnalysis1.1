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
     *
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
     *
     * @param className
     * @param methodName
     * @param methodParameters
     * @param depth            spacing denoting depth of call
     * @return
     */
    private List<String> traceMethodCalls(String className, String methodName, String methodParameters, String depth) {
        if (className.endsWith(".class"))
            className = className.split("\\.")[0];

        List<String> allInvokedMethods = new ArrayList<String>();

        List<String> data = database.getAllInvokedMethods(className, methodName, methodParameters);

        if(data == null)
            data = getInvokedMethodsFromInhertedMethod(className,methodName,methodParameters);

        if(database.isInterface(className)){
            String interfaceName = className;
            String concreteClass = getConcreteClassOfInterface(interfaceName);
            if(concreteClass != null){
                className = concreteClass;
                data = database.getAllInvokedMethods(className,methodName,methodParameters);
            }
        }

        if (data!=null)
        {
            allInvokedMethods.add(depth + className + " " + methodName + " " + methodParameters);
            Iterator<String> iterator = data.iterator();

            while (iterator.hasNext()) {
                String invokedMethod = iterator.next();
                String methodDetails[] = invokedMethod.split(" ");
                String invokedMethodClassName = methodDetails[0];
                String invokedMethodName = methodDetails[1];
                String invokedMethodParameters = methodDetails[2];
                allInvokedMethods.addAll(traceMethodCalls(invokedMethodClassName, invokedMethodName, invokedMethodParameters, depth + "\t"));
            }
        }

        return allInvokedMethods;
    }

    private String getConcreteClassOfInterface(String interfaceName){
        List<String> implementedClasses = database.getImplementedClasses(interfaceName);
        if(implementedClasses.size()==0){
            List<String> extendedInterfaces = database.getInterfacesThatExtendInterface(interfaceName);
            for(String childInterface : extendedInterfaces){
                String concreteClass = getConcreteClassOfInterface(childInterface);
                if(concreteClass != null)
                    return concreteClass;
            }
        }
        else
            return implementedClasses.get(0);

        return null;
    }

    /**
     * Method checks if any parent class has the given method
     *
     * @param className String Name of class which contains the method to be found in parent class
     * @param methodName String Name of method to be found in parent class
     * @param methodParameters String Method parameters of the method to be found in parent class
     * @return
     */
    private List<String> getInvokedMethodsFromInhertedMethod(String className, String methodName, String methodParameters){
        String parentClassName = database.getSuperClassName(className);

        if(parentClassName == null)return null;

        List<String> allInvokedMethodsOfInheritedMethod = new ArrayList<>();

        while(!parentClassName.startsWith("java")){
            allInvokedMethodsOfInheritedMethod = database.getAllInvokedMethods(parentClassName, methodName, methodParameters);
            if(allInvokedMethodsOfInheritedMethod!=null)
                return allInvokedMethodsOfInheritedMethod;
            else
                parentClassName = database.getSuperClassName(parentClassName);
        }

        return null;
    }

}
