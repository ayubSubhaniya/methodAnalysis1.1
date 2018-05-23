package org.spr.methodAnalysis;

import java.util.jar.JarEntry;

public interface ClassFileProcessable {
    boolean processClassFile(String jarName, JarEntry jarEntry);
    boolean processClassFile(String className, String classPath);
}
