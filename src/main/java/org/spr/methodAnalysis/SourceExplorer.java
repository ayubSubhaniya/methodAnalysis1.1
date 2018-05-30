package org.spr.methodAnalysis;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SourceExplorer implements DataSender, ClassFileProcessable {
    private String sourcePath;
    private DBService database;
    ParsedClassOutputter parsedClassOutputter;
    private static final Logger LOGGER = Logger.getLogger(SourceExplorer.class.getName());

    /**
     * Constructor
     *
     * @param path     String path of file or directory ot be explored
     * @param database DBService Database service object
     */
    public SourceExplorer(String path, DBService database, ParsedClassOutputter parsedClassOutputter) {
        this.sourcePath = path;
        this.database = database;
        this.parsedClassOutputter = parsedClassOutputter;
    }

    /**
     * Method checks if the given file is .class or .jar or a directory and then takes action accordingly
     *
     * @return boolean True if all files were successfully explored and false otherwise
     * @throws Exception
     */
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

    /**
     * Method explore every entity inside the directory by doing a dfs
     *
     * @param directory File directory file to be explored
     * @return boolean true if directory is successfully explored and false otherwise
     */
    private boolean exploreDirectory(File directory) throws Exception {
        String[] entries = directory.list();
        if (entries == null) {
            LOGGER.error("Directory " + directory.getName() + " cannot be explored");
            return false;
        }

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
            if (!success) {
                LOGGER.error("Directory " + directory.getName() + " cannot be explored");
                return false;
            }
        }

        return true;
    }

    /**
     * Mehtod explores every entity inside .jar file
     *
     * @param jarFile JarFile .jar file to be explored
     * @return boolean true if all jar entries of the file are successfully explored and false otherwise
     */
    private boolean exploreJar(JarFile jarFile) throws Exception {
        Enumeration enumEntries = jarFile.entries();

        while (enumEntries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumEntries.nextElement();
            if (jarEntry.getName().endsWith(".class")) {
                boolean success = processClassFileToJSON(jarFile, jarEntry);
                if (!success) {
                    LOGGER.error("Error in Processing " + jarEntry.getName());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Method sends data to database service
     *
     * @param data Object data to be sent to database service
     * @return boolean true if data was successfully sent to database service and false otherwise
     */
    public boolean sendData(Object data) throws Exception {
        return database.addData(data);
    }

    @Override
    public boolean sendData(List<? extends Object> data) throws Exception {
        return database.addData(data);
    }

    /**
     * Method gets data from ClassParserAdapter and converts it to JSONObject to send
     *
     * @param jarFile       JarFile .jar file to which the jarEntry belongs
     * @param classJarEntry JarEntry jarEntry of the .class file
     * @return boolean true if file is successfully processed and false otherwise
     */
    public boolean processClassFileToJSON(JarFile jarFile, JarEntry classJarEntry) throws Exception {
        InputStream classInputStream = null;
        try {
            classInputStream = jarFile.getInputStream(classJarEntry);
            if(parsedClassOutputter.isInterface(classInputStream))
                return processInterfaceClassFileToJSON(jarFile,classJarEntry);
            classInputStream.close();

            classInputStream = jarFile.getInputStream(classJarEntry);
            List<JSONObject> parsedClassMethods = parsedClassOutputter.getParsedMethodsInJSON(classInputStream);
            classInputStream.close();

            classInputStream = jarFile.getInputStream(classJarEntry);
            String superClassName = parsedClassOutputter.getSuperClassName(classInputStream);
            classInputStream.close();

            classInputStream = jarFile.getInputStream(classJarEntry);
            JSONArray interfacesImplemented = parsedClassOutputter.getImplementedInterfaces(classInputStream);

            String[] jarPath = jarFile.getName().split(File.separator);
            String jarName = jarPath[jarPath.length - 1];
            String className = classJarEntry.getName().split("\\.")[0];

            for (JSONObject parsedMethod : parsedClassMethods) {
                parsedMethod.put(ParsedMethodFields.CLASS_NAME, className);
                parsedMethod.put(ParsedMethodFields.SUPER_CLASS_NAME, superClassName);
                parsedMethod.put(ParsedMethodFields.IMPLEMENTED_INTERFACES, interfacesImplemented);
                parsedMethod.put(ParsedMethodFields.JAR_NAME, jarName);
                parsedMethod.put(ParsedMethodFields.TIME_STAMP, System.currentTimeMillis());
            }
            return sendData(parsedClassMethods);
        } finally {
            if (classInputStream != null)
                classInputStream.close();
        }

    }

    public boolean processInterfaceClassFileToJSON(JarFile jarFile, JarEntry interfaceJarEntry) throws Exception {
        InputStream interfaceInputStream = null;
        try {
            interfaceInputStream = jarFile.getInputStream(interfaceJarEntry);
            List<JSONObject> parsedInterfaceMethods = parsedClassOutputter.getParsedMethodsInJSON(interfaceInputStream);
            interfaceInputStream.close();

            interfaceInputStream = jarFile.getInputStream(interfaceJarEntry);
            JSONArray interfacesExtended = parsedClassOutputter.getImplementedInterfaces(interfaceInputStream);

            String[] jarPath = jarFile.getName().split(File.separator);
            String jarName = jarPath[jarPath.length - 1];
            String interfaceName = interfaceJarEntry.getName().split("\\.")[0];

            for (JSONObject parsedMethod : parsedInterfaceMethods) {
                parsedMethod.put(ParsedMethodFields.INTERFACE_NAME, interfaceName);
                parsedMethod.put(ParsedMethodFields.EXTENDED_INTERFACES, interfacesExtended);
                parsedMethod.put(ParsedMethodFields.JAR_NAME, jarName);
                parsedMethod.put(ParsedMethodFields.TIME_STAMP, System.currentTimeMillis());
            }
            return sendData(parsedInterfaceMethods);
        } finally {
            if (interfaceInputStream != null)
                interfaceInputStream.close();
        }
    }

    /**
     * Method gets data from ClassParserAdapter and converts it to JSONObject to send
     *
     * @param classPath String path of .class file which is to be processed
     * @return boolean true if file is successfully processed and false otherwise
     */
    public boolean processClassFileToJSON(String classPath) throws Exception {
        InputStream classInputStream = null;
        try {
            classInputStream = new FileInputStream(classPath);
            if(parsedClassOutputter.isInterface(classInputStream))
                return processInterfaceClassFileToJSON(classPath);
            classInputStream.close();

            classInputStream = new FileInputStream(classPath);
            List<JSONObject> parsedClassMethods = parsedClassOutputter.getParsedMethodsInJSON(classInputStream);
            classInputStream.close();

            classInputStream = new FileInputStream(classPath);
            String relativeClassPath = parsedClassOutputter.getRelativeClassPath(classInputStream);
            classInputStream.close();

            classInputStream = new FileInputStream(classPath);
            String superClassName = parsedClassOutputter.getSuperClassName(classInputStream);
            classInputStream.close();

            classInputStream = new FileInputStream(classPath);
            JSONArray interfacesImplemented = parsedClassOutputter.getImplementedInterfaces(classInputStream);

            for (JSONObject parsedMethod : parsedClassMethods) {
                parsedMethod.put(ParsedMethodFields.CLASS_NAME, relativeClassPath);
                parsedMethod.put(ParsedMethodFields.SUPER_CLASS_NAME, superClassName);
                parsedMethod.put(ParsedMethodFields.IMPLEMENTED_INTERFACES, interfacesImplemented);
                parsedMethod.put(ParsedMethodFields.TIME_STAMP, System.currentTimeMillis());
            }
            return sendData(parsedClassMethods);
        } finally {
            if (classInputStream != null)
                classInputStream.close();
        }

    }

    public boolean processInterfaceClassFileToJSON(String interfacePath) throws Exception {
        InputStream interfaceInputStream = null;
        try {
            interfaceInputStream = new FileInputStream(interfacePath);
            List<JSONObject> parsedInterfaceMethods = parsedClassOutputter.getParsedMethodsInJSON(interfaceInputStream);
            interfaceInputStream.close();

            interfaceInputStream = new FileInputStream(interfacePath);
            String relativeInterfacePath = parsedClassOutputter.getRelativeClassPath(interfaceInputStream);
            interfaceInputStream.close();

            interfaceInputStream = new FileInputStream(interfacePath);
            JSONArray interfacesImplemented = parsedClassOutputter.getImplementedInterfaces(interfaceInputStream);

            for (JSONObject parsedMethod : parsedInterfaceMethods) {
                parsedMethod.put(ParsedMethodFields.INTERFACE_NAME, relativeInterfacePath);
                parsedMethod.put(ParsedMethodFields.EXTENDED_INTERFACES, interfacesImplemented);
                parsedMethod.put(ParsedMethodFields.TIME_STAMP, System.currentTimeMillis());
            }
            return sendData(parsedInterfaceMethods);
        } finally {
            if (interfaceInputStream != null)
                interfaceInputStream.close();
        }
    }

}