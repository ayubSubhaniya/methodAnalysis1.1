package org.spr.methodAnalysis;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class ClassParser {

    private final Printer printer = new Textifier();
    private final TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);
    private InputStream inputStream;

    public ClassParser(InputStream inputStream){
        this.inputStream=inputStream;
    }

    public Map<String, List<String>> getMethodEntriesWithInvokedMethods(){

    }

    private List<String> getInvokedMethods(MethodNode methodNode){

    }

    private String instructionToString(AbstractInsnNode instructionNode) {
        instructionNode.accept(traceMethodVisitor);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString();
    }
}
