package org.spr.methodAnalysis;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ParsedClassOutputter {
    List<JSONObject> getParsedMethodsInJSON(InputStream classInputStream) throws IOException;

    String getRelativeClassPath(InputStream InputStream) throws IOException;
}
