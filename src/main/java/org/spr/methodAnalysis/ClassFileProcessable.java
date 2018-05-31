package org.spr.methodAnalysis;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface ClassFileProcessable {
    boolean processClassFileToJSON(JarFile jarFile, JarEntry classJarEntry) throws Exception;

    boolean processClassFileToJSON(String classPath) throws Exception;

    boolean processInterfaceClassFileToJSON(JarFile jarFile, JarEntry classJarEntry) throws Exception;

    boolean processInterfaceClassFileToJSON(String classPath) throws Exception;

}