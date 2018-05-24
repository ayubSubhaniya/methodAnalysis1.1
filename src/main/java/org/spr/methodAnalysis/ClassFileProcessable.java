package org.spr.methodAnalysis;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface ClassFileProcessable {
    boolean processClassFileToJSON(JarFile jarFile, JarEntry classJarEntry) throws Exception;
    boolean processClassFileToJSON(String classPath) throws Exception;
}
