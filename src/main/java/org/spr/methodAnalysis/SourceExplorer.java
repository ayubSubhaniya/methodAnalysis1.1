package org.spr.methodAnalysis;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SourceExplorer implements DataSendable, ClassFileProcessable {
    private String sourcePath;
    private DBInteractable database;
    ClassParserAdapter classParserAdapter;
    private static final Logger LOGGER = Logger.getLogger(SourceExplorer.class.getName());

    public SourceExplorer(String path, DBInteractable database) {
        this.sourcePath = path;
        this.database = database;
        classParserAdapter = new ClassParserAdapter();
    }

    public boolean startExploring() throws Exception {
        boolean success;
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
                IOException exception = new IOException("Provided argument must be a .class, .jar or a directory");
                LOGGER.error(exception);
                throw exception;
            }
        }
        return success;
    }

    private boolean exploreDirectory(File directory) throws Exception {
        String[] entries = directory.list();
        if(entries==null)
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
            if (!success){
                LOGGER.error("Directory cannot be explored");
                return false;
            }
        }
        return true;
    }

    private boolean exploreJar(JarFile jarFile) throws Exception {
        Enumeration enumEntries = jarFile.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                boolean success = processClassFileToJSON(jarFile, jarEntry);
                if (!success) {
                    LOGGER.error("Data cannot be processed");
                    return false;
                }
            }
        }
        return true;
    }

    public boolean sendData(Object data) throws Exception {
        return database.addData(data);
    }

    public boolean processClassFileToJSON(JarFile jarFile, JarEntry classJarEntry) throws Exception {
        InputStream classInputStream = jarFile.getInputStream(classJarEntry);
        ArrayList<JSONObject> parsedClassMethods = classParserAdapter.getParsedMethodsInJSON(classInputStream);
        if (parsedClassMethods.isEmpty()) return true;
        String[] jarPath = jarFile.getName().split(File.separator);
        String jarName = jarPath[jarPath.length - 1];
        String className = classJarEntry.getName();
        className = className.split("\\.")[0];
        for (JSONObject parsedMethod : parsedClassMethods) {
            parsedMethod.put(ParsedMethodFields.CLASS_NAME, className);
            parsedMethod.put(ParsedMethodFields.JAR_NAME, jarName);
            parsedMethod.put(ParsedMethodFields.TIME_STAMP, System.currentTimeMillis());
        }

        return sendData(parsedClassMethods);
    }

    public boolean processClassFileToJSON(String classPath) throws Exception {
        InputStream classInputStream = new FileInputStream(classPath);
        ArrayList<JSONObject> parsedClassMethods = classParserAdapter.getParsedMethodsInJSON(classInputStream);
        if (parsedClassMethods.isEmpty()) return true;
        String className = classPath.split("\\.")[0];
        for (JSONObject parsedMethod : parsedClassMethods) {
            parsedMethod.put(ParsedMethodFields.CLASS_NAME, className);
            parsedMethod.put(ParsedMethodFields.TIME_STAMP, System.currentTimeMillis());
        }
        return sendData(parsedClassMethods);
    }
}
