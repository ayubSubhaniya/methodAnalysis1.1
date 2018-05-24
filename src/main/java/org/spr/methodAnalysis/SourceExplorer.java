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

    /**
     * Constructor
     * @param path String path of file or directory ot be explored
     * @param database  DBInteractable Database service object
     */
    public SourceExplorer(String path, DBInteractable database) {
        this.sourcePath = path;
        this.database = database;
        classParserAdapter = new ClassParserAdapter();
    }

    /**
     * Method checks if the given file is .class or .jar or a directory and then takes action accordingly
     * @return  boolean True if all files were successfully explored and false otherwise
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
     * @param directory File directory file to be explored
     * @return boolean true if directory is successfully explored and false otherwise
     */
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

    /**
     * Mehtod explores every entity inside .jar file
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
                    LOGGER.error("Data cannot be processed");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method sends data to database service
     * @param data Object data to be sent to database service
     * @return  boolean true if data was successfully sent to database service and false otherwise
     */
    public boolean sendData(Object data) throws Exception {
        return database.addData(data);
    }

    /**
     * Method gets data from ClassParserAdapter and converts it to JSONObject to send
     * @param jarFile JarFile .jar file to which the jarEntry belongs
     * @param classJarEntry JarEntry jarEntry of the .class file
     * @return boolean true if file is successfully processed and false otherwise
     */
    public boolean processClassFileToJSON(JarFile jarFile, JarEntry classJarEntry) throws Exception {
        InputStream classInputStream = jarFile.getInputStream(classJarEntry);
        ArrayList<JSONObject> parsedClassMethods = classParserAdapter.getParsedMethodsInJSON(classInputStream);
        if (parsedClassMethods.isEmpty()) return true;
        String[] jarPath = jarFile.getName().split(File.separator);
        String jarName = jarPath[jarPath.length - 1];
        String className = classJarEntry.getName().split("\\.")[0];
        for (JSONObject parsedMethod : parsedClassMethods) {
            parsedMethod.put(ParsedMethodFields.CLASS_NAME, className);
            parsedMethod.put(ParsedMethodFields.JAR_NAME, jarName);
            parsedMethod.put(ParsedMethodFields.TIME_STAMP, System.currentTimeMillis());
        }

        return sendData(parsedClassMethods);
    }

    /**
     * Method gets data from ClassParserAdapter and converts it to JSONObject to send
     * @param classPath String path of .class file which is to be processed
     * @return boolean true if file is successfully processed and false otherwise
     */
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
