package org.spr.methodAnalysis;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SourceExplorer implements DataSendable, ClassFileProcessable {
    private String sourcePath;
    private DBInteractable database;
    ClassParserAdapter classParserAdapter;

    public SourceExplorer(String path, DBInteractable database) {
        this.sourcePath = path;
        this.database = database;
        classParserAdapter = new ClassParserAdapter();
    }

    public boolean startExploring() throws IOException {
        boolean success = true;
        if (sourcePath.endsWith(".class")) {
            success = processClassFileToJSON(sourcePath);
        } else if (sourcePath.endsWith(".jar")) {
            JarFile jar = new JarFile(sourcePath);
            success = exploreJar(jar);
        } else {
            File f = new File(sourcePath);
            if (f.isDirectory()) {
                success = exploreDirectory(f);
            } else {
                // add to logger    System.out.println("Provided argument must be a .class, .jar or a directory");
                throw new IOException();
            }
        }
        return success;
    }

    private boolean exploreDirectory(File directory) throws IOException {
        String[] entries = directory.list();

        if (entries == null)
            throw new NullPointerException();

        for (String entry : entries) {
            boolean success = true;
            if (entry.endsWith(".jar")) {
                JarFile jar = new JarFile(directory.getAbsolutePath() + File.separator + entry);
                success = exploreJar(jar);

            } else if (entry.endsWith(".class"))
                success = processClassFileToJSON(directory.getAbsolutePath() + File.separator + entry);
            else {
                File f = new File(directory.getAbsolutePath() + File.separator + entry);
                if (f.isDirectory())
                    success = exploreDirectory(f);
            }
            if (!success)
                return false;
        }
        return true;
    }

    private boolean exploreJar(JarFile jarFile) throws IOException {
        String[] jarPath = jarFile.getName().split(File.separator);
        String jarName = jarPath[jarPath.length - 1];

        Enumeration enumEntries = jarFile.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                InputStream classInputStream = jarFile.getInputStream(jarEntry);
                boolean success = processClassFileToJSON(jarFile, jarEntry);
                if (!success) {
                    // add to logger
                    return false;
                }
            }
        }
        return true;
    }

    public boolean sendData(Object data) {
        // add data to database
        return false;
    }

    public boolean processClassFileToJSON(JarFile jarFile, JarEntry classJarEntry) throws IOException {
        InputStream classInputStream = jarFile.getInputStream(classJarEntry);
        ArrayList<JSONObject> parsedClassMethods = classParserAdapter.getParsedMethodsInJSON(classInputStream);
        String[] jarPath = jarFile.getName().split(File.separator);
        String jarName = jarPath[jarPath.length - 1];
        String className = classJarEntry.getName();
        className = className.split("\\.")[0];
        for (JSONObject parsedMethod : parsedClassMethods) {
            parsedMethod.put("className", className);
            parsedMethod.put("jarName", jarName);
        }
        return sendData(parsedClassMethods);
    }

    public boolean processClassFileToJSON(String classPath) throws IOException {
        InputStream classInputStream = new FileInputStream(classPath);
        ArrayList<JSONObject> parsedClassMethods = classParserAdapter.getParsedMethodsInJSON(classInputStream);
        String className = classPath.split("\\.")[0];
        for (JSONObject parsedMethod : parsedClassMethods) {
            parsedMethod.put("className", className);
        }
        return sendData(parsedClassMethods);
    }
}
