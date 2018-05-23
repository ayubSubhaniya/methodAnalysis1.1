package org.spr.methodAnalysis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClassParserAdapter {
    private ClassParser classParser;

    public ClassParserAdapter() {
        classParser = new ClassParser();
    }

    public ArrayList<JSONObject> getParsedMethodsInJSON(InputStream classInputStream) throws IOException {
        Map<String, List<String>> parsedMethods = classParser.getMethodEntriesWithInvokedMethods(classInputStream);
        ArrayList<JSONObject> parsedMethodsInJSON = new ArrayList<JSONObject>();
        Iterator<String> iterator = parsedMethods.keySet().iterator();
        while (iterator.hasNext()) {
            JSONObject parsedMethodJSONObject = new JSONObject();
            String methodName = iterator.next();
            parsedMethodJSONObject.put("methodName", methodName);
            JSONArray invokedMethods = new JSONArray();
            for (String invokedMethod : parsedMethods.get(methodName))
                invokedMethods.put(invokedMethod);
            parsedMethodJSONObject.put("invokedMethods", invokedMethods);
            parsedMethodsInJSON.add(parsedMethodJSONObject);
        }
        return parsedMethodsInJSON;
    }

}
