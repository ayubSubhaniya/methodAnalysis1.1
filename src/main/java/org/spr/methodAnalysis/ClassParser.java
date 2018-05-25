package org.spr.methodAnalysis;

import org.objectweb.asm.ClassReader;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassParser {
    private final Printer printer = new Textifier();
    private final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);

    /**
     * Method parses the file in InputStream
     *
     * @param inputStream InputStream of the file to be parsed
     * @return Map<String       ,               List       <       String>> Name of method as Key and the invokedMethods by that method as List<String> as value in a Map
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
     * @return List<string> of the invokedMethods
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

}
