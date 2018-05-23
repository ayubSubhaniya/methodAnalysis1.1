package org.spr.methodAnalysis;

import java.util.jar.JarEntry;

public interface ClassFileProcessable {
    boolean processClassFileToJSON(String jarName, JarEntry jarEntry);
    boolean processClassFileToJSON(String classPath);
}
