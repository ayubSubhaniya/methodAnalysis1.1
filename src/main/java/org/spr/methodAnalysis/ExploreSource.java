package org.spr.methodAnalysis;

import java.io.File;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExploreSource implements DataSendable,ClassFileProcessable {
    private String path;
    private DBInteractable database;
    public ExploreSource(String path, DBInteractable database){
        this.path=path;
        this.database=database;
    }

    public startExploring(){

    }

    private boolean exploreDirectory(File directory){

    }

    private boolean exploreJar(JarFile jarFile){

    }

    public boolean sendData(Object data) {
        return false;
    }

    public boolean processClassFile(String jarName, JarEntry jarEntry) {
        return false;
    }

    public boolean processClassFile(String className, String classPath) {
        return false;
    }
}
