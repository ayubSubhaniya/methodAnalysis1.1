package org.spr.methodAnalysis;

import org.apache.log4j.Logger;

import java.io.IOException;

public class AnalyseMethod {
    private static final Logger LOGGER = Logger.getLogger(AnalyseMethod.class.getName());

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            IOException exception = new IOException("No Arguments Provided");
            LOGGER.error(exception);
            throw exception;
        }

        ElasticSearchService elasticSearchService;

        if (args.length >= 6) {
            String hostname = args[1];
            int port = Integer.parseInt(args[2]);
            String scheme = args[3];
            elasticSearchService = new ElasticSearchService(hostname, port, scheme);
            elasticSearchService.createIndex(args[4], args[5]);
        } else if (args.length >= 3) {
            elasticSearchService = new ElasticSearchService();
            elasticSearchService.createIndex(args[1], args[2]);
        } else {
            IOException exception = new IOException("Index Name and Document Type not provided");
            LOGGER.error(exception);
            throw exception;
        }

        String sourcePath = args[0];

        try {
            SourceExplorer sourceExplorer = new SourceExplorer(sourcePath, elasticSearchService);
            boolean success = sourceExplorer.startExploring();
            sourceExplorer.traceMethodCalls("java/lang/Double","toString"," ");
            if (success)
                LOGGER.info("Source explored and added to database");
            else
                LOGGER.info("Error in exploring source");
        } catch (Exception e) {
            LOGGER.error(e.fillInStackTrace());
        } finally {
            elasticSearchService.closeConnection();
        }

    }
}