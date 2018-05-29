package org.spr.methodAnalysis;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;


public class ParserAndAnalyserMain {
    private static final Logger LOGGER = Logger.getLogger(ParserAndAnalyserMain.class.getName());

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            IOException exception = new IOException("No Arguments Provided");
            LOGGER.error(exception);
            throw exception;
        }

        switch (args[0]) {
            case "parseMethods":
                handleParseMethodsRequest(args);
                break;
            case "analyseMethod":
                handleAnalyseMethodRequest(args);
                break;
            default:
                IllegalArgumentException exception = new IllegalArgumentException("Command not supported.");
                LOGGER.error(exception);
                throw exception;
        }
    }

    /**
     * Handle request for parsing all methods inside source
     * @param args
     * @throws IOException
     */
    private static void handleParseMethodsRequest(String args[]) throws IOException {
        ElasticSearchService elasticSearchService;
        ClassParser classParser = new ClassParser();
        String sourcePath;
        boolean indexAdded;

        if (args.length >= 6) {
            String hostname = args[1];
            int port = Integer.parseInt(args[2]);
            String scheme = args[3];

            elasticSearchService = new ElasticSearchService(hostname, port, scheme);
            indexAdded = elasticSearchService.createIndex(args[4], args[5]);

            sourcePath = args[6];

        } else if (args.length >= 3) {
            elasticSearchService = new ElasticSearchService();
            indexAdded = elasticSearchService.createIndex(args[1], args[2]);

            sourcePath = args[3];
        } else {
            IOException exception = new IOException("Index Name and Document Type not provided");
            LOGGER.error(exception);
            throw exception;
        }

        try {

            if (!indexAdded) {
                RuntimeException indexException = new RuntimeException("Cannot add index");
                LOGGER.error(indexException);
                throw indexException;
            }

            SourceExplorer sourceExplorer = new SourceExplorer(sourcePath, elasticSearchService, classParser);
            boolean success = sourceExplorer.startExploring();
            if (success)
                LOGGER.info("Source explored and added to database");
            else
                LOGGER.info("Error in exploring source");

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            elasticSearchService.closeConnection();
        }
    }

    /**
     * Handle request for analysing a methods
     * @param args
     * @throws IOException
     */
    private static void handleAnalyseMethodRequest(String args[]) throws IOException {
        ElasticSearchService elasticSearchService;
        String className, methodName, methodParameters;
        boolean indexExist;

        if (args.length >= 9) {
            String hostname = args[1];
            int port = Integer.parseInt(args[2]);
            String scheme = args[3];
            elasticSearchService = new ElasticSearchService(hostname, port, scheme);
            indexExist = elasticSearchService.setIndex(args[4], args[5]);

            className = args[6];
            methodName = args[7];
            methodParameters = args[8];

        } else if (args.length >= 6) {
            elasticSearchService = new ElasticSearchService();
            indexExist = elasticSearchService.setIndex(args[1], args[2]);

            className = args[3];
            methodName = args[4];
            methodParameters = args[5];
        } else {
            IOException exception = new IOException("IndexName Doc Type ClassName MethodName MethodParameters not provided");
            LOGGER.error(exception);
            throw exception;
        }

        try {
            if (!indexExist) {
                IllegalArgumentException indexException = new IllegalArgumentException("Index does not exist");
                LOGGER.error(indexException);
                throw indexException;
            }

            MethodAnalyser methodAnalyser = new MethodAnalyser(elasticSearchService);
            List<String> allMethodcalls = methodAnalyser.traceMethodCalls(className, methodName, methodParameters);
            for (String method : allMethodcalls)
                System.out.println(method);
        } finally {
            elasticSearchService.closeConnection();
        }
    }
}