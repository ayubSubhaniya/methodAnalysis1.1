package org.spr.methodAnalysis;

import java.util.List;

public interface DBService {
    boolean addData(Object data) throws Exception;

    boolean addData(List<? extends Object> data) throws Exception;

    List<String> getAllInvokedMethods(String className, String methodName, String methodParameters);

    List<String> getImplementedClasses(String interfaceName);

    List<String> getExtendedInterfaces(String interfaceName);

    List<String> getInterfacesThatExtendInterface(String interfaceName);

    String getSuperClassName(String className);

    int getNumberOfInterfaceImplementations(String interfaceName);

    boolean isInterface(String InterfaceName);
}