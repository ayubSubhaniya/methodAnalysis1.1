package org.spr.methodAnalysis;

public class JUnitTestClass implements Runnable {
    public static final String mockMethodName = "mockMethod";
    public static final String handlerMethodName = "handlerMethod";
    public static final String interfaceMethodName = "run";
    public static final String staticMockMethodName = "staticMockMethod";

    static {
        staticMockMethod();
    }

    public JUnitTestClass() {
        mockMethod();
    }

    public void run() {
        mockMethod();
    }

    public void testForFinally() {
        try {
            //doing something
        } finally {
            mockMethod();
        }
    }

    public void testForCatch() {
        try {
            mockMethod();
        } catch (Exception e) {
            handlerMethod();
        }
    }

    public void testForTry() {
        try {
            mockMethod();
        } catch (Exception e) {
            handlerMethod();
        }
    }

    private void testForPrivateMethod() {
        mockMethod();
    }

    private void testForStaticMethod() {
        mockMethod();
    }

    public void mockMethod() {
        System.out.println("Inside Mock Method");
    }

    public void handlerMethod() {
        System.out.println("Inside Test Method");
    }

    public static void staticMockMethod() { System.out.println("Inside static Mock Method"); }
}
