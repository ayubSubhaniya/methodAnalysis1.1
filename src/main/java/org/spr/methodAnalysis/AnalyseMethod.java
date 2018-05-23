package org.spr.methodAnalysis;

import java.io.IOException;

public class AnalyseMethod {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            //add to logger System.out.println("Arguments Not Provided");
            throw new IOException();
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
        } else
            throw new IOException();

        String sourcePath = args[0];
        SourceExplorer sourceExplorer = new SourceExplorer(sourcePath,elasticSearchService);
        boolean success = sourceExplorer.startExploring();
        if(!success){
            // add to logger
        }
    }

}