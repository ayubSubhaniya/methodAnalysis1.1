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
        Iterator<Map.Entry<String, List<String>>> iterator = parsedMethods.entrySet().iterator();
        while (iterator.hasNext()) {
            JSONObject parsedMethodJSONObject = new JSONObject();
            Map.Entry<String, List<String>> parsedMethod = iterator.next();
            String methodName = parsedMethod.getKey();
            parsedMethodJSONObject.put(ParsedMethodFields.METHOD_NAME, methodName);
            JSONArray invokedMethodsJSONArray = new JSONArray();
            List<String> invokedMethods = parsedMethod.getValue();
            for (String invokedMethod : invokedMethods)
                invokedMethodsJSONArray.put(invokedMethod);
            parsedMethodJSONObject.put(ParsedMethodFields.INVOKED_METHODS, invokedMethodsJSONArray);
            parsedMethodsInJSON.add(parsedMethodJSONObject);
        }
        return parsedMethodsInJSON;
    }
}
