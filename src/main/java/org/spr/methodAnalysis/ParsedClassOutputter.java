package org.spr.methodAnalysis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ParsedClassOutputter {
    List<JSONObject> getParsedMethodsInJSON(InputStream classInputStream) throws IOException;

    String getRelativeClassPath(InputStream classInputStream) throws IOException;

    String getSuperClassName(InputStream classInputStream) throws  IOException;

    JSONArray getImplementedInterfaces(InputStream classInputStream) throws IOException;

    boolean isInterface(InputStream inputStream) throws IOException;
}
