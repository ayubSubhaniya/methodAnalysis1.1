package org.spr.methodAnalysis;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class TestClassParser {
    private static ClassParserAdapter classParserAdapter;
    private final String mockMethodName = JUnitTestClass.mockMethodName;
    private final String handlerMethodName = JUnitTestClass.handlerMethodName;
    private final String interfaceMethod = JUnitTestClass.interfaceMethodName;
    private final String staticMockMethodName = JUnitTestClass.staticMockMethodName;
    private final String className = "JUnitTestClass.class";
    private static final Logger LOGGER = Logger.getLogger(AnalyseMethod.class.getName());

    @BeforeClass
    public static void initialise() {
        classParserAdapter = new ClassParserAdapter();
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testForInterface() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                interfaceMethod,
        };
        final String invokedMethodNames[] = new String[]{
                mockMethodName
        };

        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }

    }

    @Test
    public void testForConstructor() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                "<init>",
        };

        final String invokedMethodNames[] = new String[]{
                mockMethodName

        };
        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @Test
    public void testForTry() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                "testForTry"
        };

        final String invokedMethodNames[] = new String[]{
                mockMethodName

        };
        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @Test
    public void testForCatch() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                "testForCatch"
        };

        final String invokedMethodNames[] = new String[]{
                handlerMethodName
        };

        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @Test
    public void testForFinally() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                "testForFinally"
        };

        final String invokedMethodNames[] = new String[]{
                mockMethodName
        };

        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @Test
    public void testForPrivateMethod() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                "testForPrivateMethod"
        };

        final String invokedMethodNames[] = new String[]{
                mockMethodName
        };

        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @Test
    public void testForStaticMethod() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                "testForStaticMethod"
        };

        final String invokedMethodNames[] = new String[]{
                mockMethodName
        };

        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    @Test
    public void testForStaticBlock() {
        final String path = JUnitTestClass.class.getResource(className).getPath();
        final String methodNames[] = new String[]{
                "<clinit>",
        };
        final String invokedMethodNames[] = new String[]{
               staticMockMethodName,
        };

        try {
            boolean isMethodVisited[] = areMethodsVisited(path, methodNames, invokedMethodNames);
            for (int i = 0; i < isMethodVisited.length; i++)
                assertTrue(methodNames[i] + " not visited", isMethodVisited[i]);
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }

    }

    private boolean[] areMethodsVisited(String path, String[] methodNames, String[] invokedMethodNames) throws IOException {
        boolean isMethodVisited[] = new boolean[methodNames.length];
        ArrayList<JSONObject> jsonObj;
        final InputStream inputStream = new FileInputStream(path);
        jsonObj = classParserAdapter.getParsedMethodsInJSON(inputStream);
        for (JSONObject obj : jsonObj) {
            String methodName = (String) obj.get(ParsedMethodFields.METHOD_NAME);
            int idx = -1;
            for (int i = 0; i < methodNames.length; i++)
                if (methodName.contains(methodNames[i])) {
                    idx = i;
                    break;
                }

            if (idx != -1) {
                JSONArray invokedMethods = obj.getJSONArray(ParsedMethodFields.INVOKED_METHODS);
                for (Object o : invokedMethods) {
                    if (o.toString().contains(invokedMethodNames[idx])) {
                        isMethodVisited[idx] = true;
                        break;
                    }
                }
            }

        }
        return isMethodVisited;
    }
}
