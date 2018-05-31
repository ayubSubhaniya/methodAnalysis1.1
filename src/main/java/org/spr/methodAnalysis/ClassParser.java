package org.spr.methodAnalysis;

import jdk.internal.util.xml.impl.Input;
import org.json.JSONArray;
import org.json.JSONObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class ClassParser implements ParsedClassOutputter {
    private final Printer printer = new Textifier();
    private final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);

    /**
     * Method parses the file in InputStream
     *
     * @param inputStream InputStream of the file to be parsed
     * @return Map<String                               ,                               List               <               String> > Name of method as Key and the invokedMethods by that method as List<String> as value in a Map
     */
    public Map<String, List<String>> getMethodEntriesWithInvokedMethods(InputStream inputStream) throws IOException {
        ClassReader reader = new ClassReader(inputStream);

        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        final List<MethodNode> methods = classNode.methods;
        HashMap<String, List<String>> methodEntriesWithInvokedMethods = new HashMap<String, List<String>>();

        for (MethodNode methodNode : methods) {
            List<String> invokedMethods = getInvokedMethods(methodNode);
            methodEntriesWithInvokedMethods.put(methodNode.name.trim() + " " + methodNode.desc.trim(), invokedMethods);
        }

        return methodEntriesWithInvokedMethods;
    }

    /**
     * Method retrieves invokedMethods in the given Method
     *
     * @param methodNode MethodNode to be parsed
     * @return List<String> of the invokedMethods
     */
    private List<String> getInvokedMethods(MethodNode methodNode) {
        List<String> invokedMethods = new ArrayList<String>();
        InsnList instructionList = methodNode.instructions;

        for (int i = 0; i < instructionList.size(); i++) {
            String instruction = instructionToString(instructionList.get(i));
            instruction = instruction.trim();

            if (instruction.startsWith("INVOKE") && !instruction.startsWith("INVOKEDYNAMIC")) {
                String[] subInstruction = instruction.split(" ");
                String[] methodDetails = subInstruction[1].split("\\.");

                String methodClassName = methodDetails[0];
                String methodName = methodDetails[1];
                String methodDescription = subInstruction[2];

                invokedMethods.add(methodClassName + " " + methodName + " " + methodDescription);
            }

        }

        return invokedMethods;
    }

    /**
     * Method converts the bytecode instruction to String
     *
     * @param instructionNode AbstractInsnNode of the instruction to be converted to String
     * @return Instruction in String format
     */
    private String instructionToString(AbstractInsnNode instructionNode) {
        instructionNode.accept(traceMethodVisitor);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();

        return sw.toString();
    }

    /**
     * Method gets data from ClassParser and parses it to JSONObject
     *
     * @param classInputStream InputStream of the file to be parsed
     * @return ArrayList<JSONObject> parsed data converted in JSON format
     */
    public List<JSONObject> getParsedMethodsInJSON(InputStream classInputStream) throws IOException {
        Map<String, List<String>> parsedMethods = getMethodEntriesWithInvokedMethods(classInputStream);
        List<JSONObject> parsedMethodsInJSON = new ArrayList<JSONObject>();

        Iterator<Map.Entry<String, List<String>>> iterator = parsedMethods.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> parsedMethod = iterator.next();
            String[] methodDetails = parsedMethod.getKey().split(" ");
            String methodName = methodDetails[0];
            String methodParameters = methodDetails[1];

            JSONObject parsedMethodJSONObject = new JSONObject();
            parsedMethodJSONObject.put(ParsedMethodFields.METHOD_NAME, methodName);
            parsedMethodJSONObject.put(ParsedMethodFields.METHOD_PARAMETER, methodParameters);

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
     *
     * @param classInputStream InputStream of the file whose relative path is to be found
     * @return String relative path of the file
     * @throws IOException
     */
    public String getRelativeClassPath(InputStream classInputStream) throws IOException {
        ClassReader reader = new ClassReader(classInputStream);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        return classNode.name;
    }

    /**
     * Method finds the super class name
     *
     * @param classInputStream InputStream of the class whose super class name is to be found
     * @return String super class name of the class whose InputStream is provided
     * @throws IOException
     */
    public String getSuperClassName(InputStream classInputStream) throws IOException {
        ClassReader reader = new ClassReader(classInputStream);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        return classNode.superName;
    }

    public JSONArray getImplementedInterfaces(InputStream inputStream) throws IOException {
        JSONArray implementedInterfaces = new JSONArray();

        ClassReader reader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        for (String interfaceName : classNode.interfaces)
            implementedInterfaces.put(interfaceName);

        return implementedInterfaces;
    }

    public boolean isInterface(InputStream inputStream) throws IOException {
        ClassReader reader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
    }

}