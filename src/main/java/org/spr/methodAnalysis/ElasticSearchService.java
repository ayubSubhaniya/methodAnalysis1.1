package org.spr.methodAnalysis;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.*;

import java.io.IOException;

public class ElasticSearchService implements DBInteractable {

    private RestHighLevelClient client;
    private String hostname = "localhost";
    private int port = 9200;
    private String scheme = "http";
    private String indexname;
    private String type;

    /**
     * This will connect to local elastic search database
     */
    public ElasticSearchService() {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(hostname, port, scheme)));
    }

    /**
     *
     * @param hostname hostname of ElasticSearch database
     * @param port port of ElasticSearch database
     * @param scheme network routing scheme
     */
    public ElasticSearchService(String hostname, int port, String scheme) {
        this.hostname = hostname;
        this.port = port;
        this.scheme = scheme;
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(this.hostname, this.port, this.scheme)));
    }

    /**
     * Method creates Index for Elastic Search
     * @param indexName
     * @param type
     */
    public boolean createIndex(String indexName, String type) {
        this.indexname = indexName;
        this.type = type;

        try {
            if (!isExistingIndex(indexname)) {
                CreateIndexRequest request = new CreateIndexRequest(indexName);
                CreateIndexResponse response = client.indices()
                        .create(request);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addData(Object data) {
        return false;
    }

    /**
     * Check if index already exist in databaase
     * @param index name of index
     * @return boolean true if index exist else false
     */
    public boolean isExistingIndex(String index) {
        try {
            Response restResponse = client.getLowLevelClient().performRequest("GET", "/" + index);
            //System.out.println(restResponse.getStatusLine());
            return true;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
