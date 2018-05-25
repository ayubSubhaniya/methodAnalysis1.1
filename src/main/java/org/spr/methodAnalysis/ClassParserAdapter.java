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

    /**
     * Method gets data from ClassParser and parses it to JSONObject
     *
     * @param classInputStream InputStream of the file to be parsed
     * @return ArrayList<JSONObject> parsed data converted in JSON format
     */
    public ArrayList<JSONObject> getParsedMethodsInJSON(InputStream classInputStream) throws IOException {
        Map<String, List<String>> parsedMethods = classParser.getMethodEntriesWithInvokedMethods(classInputStream);
        ArrayList<JSONObject> parsedMethodsInJSON = new ArrayList<JSONObject>();

        Iterator<Map.Entry<String, List<String>>> iterator = parsedMethods.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> parsedMethod = iterator.next();
            String[] methodDetails = parsedMethod.getKey().split(" ");
            String methodName = methodDetails[0];
            String methodParameters = methodDetails[1];

            JSONObject parsedMethodJSONObject = new JSONObject();
            parsedMethodJSONObject.put(ParsedMethodFields.METHOD_NAME, methodName);
            parsedMethodJSONObject.put(ParsedMethodFields.METHOD_PARAMETERS, methodParameters);

            JSONArray invokedMethodsJSONArray = new JSONArray();
            List<String> invokedMethods = parsedMethod.getValue();
            for (String invokedMethod : invokedMethods)
                invokedMethodsJSONArray.put(invokedMethod);

            parsedMethodJSONObject.put(ParsedMethodFields.INVOKED_METHODS, invokedMethodsJSONArray);

            parsedMethodsInJSON.add(parsedMethodJSONObject);
        }

        return parsedMethodsInJSON;
    }

    /**
     * Method finds the relative path of file of which InputStream is provided
     * @param InputStream InputStream of the file whose relative path is to be found
     * @return String relative path of the file
     * @throws IOException
     */
    public String getRelativeClassPath(InputStream InputStream) throws IOException {
        return classParser.getRelativeClassPath(InputStream);
    }

}
