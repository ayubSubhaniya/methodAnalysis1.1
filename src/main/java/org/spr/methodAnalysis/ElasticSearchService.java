package org.spr.methodAnalysis;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

public class ElasticSearchService implements DBInteractable {
    private static final int MAX_BUFFER_SIZE = (int) 1e5;
    private static final Logger LOGGER = Logger.getLogger(ElasticSearchService.class.getName());
    private RestHighLevelClient client;
    private String hostname = "localhost";
    private int port = 9200;
    private String scheme = "http";
    private String indexname;
    private String type;
    private Queue<IndexRequest> buffer;

    /**
     * This will connect to local elastic search database with
     *
     * @hostname = 9200 and
     * @port = 9200
     */
    public ElasticSearchService() {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(hostname, port, scheme)));
        buffer = new LinkedList<IndexRequest>();
        LOGGER.info("ElasticSearch Database connected");
    }

    /**
     * @param hostname hostname of ElasticSearch database
     * @param port     port of ElasticSearch database
     * @param scheme   network routing scheme
     */
    public ElasticSearchService(String hostname, int port, String scheme) {
        this.hostname = hostname;
        this.port = port;
        this.scheme = scheme;
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(hostname, port, scheme)));
        buffer = new LinkedList<IndexRequest>();
        LOGGER.info("ElasticSearch Database connected");
    }

    /**
     * Method creates Index for Elastic Search
     *
     * @param indexName
     * @param type
     */
    public boolean createIndex(String indexName, String type) {
        try {
            this.indexname = indexName;
            this.type = type;
            if (!isExistingIndex(indexname)) {
                CreateIndexRequest request = new CreateIndexRequest(indexName);
                request.mapping(type, getMapping(), XContentType.JSON);
                CreateIndexResponse response = client.indices()
                        .create(request);
                LOGGER.info("Index " + indexName + " added.");
                LOGGER.info(response.toString());
            } else
                LOGGER.info("Index already exists.");

            return true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Create mapping for adding method data
     *
     * @return JSONObject String
     * @throws IOException
     */
    private String getMapping() throws IOException {
        XContentBuilder mapping = new XContentFactory().jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject(ParsedMethodFields.CLASS_NAME)
                .field("type", "keyword")
                .endObject()
                .startObject(ParsedMethodFields.METHOD_NAME)
                .field("type", "keyword")
                .endObject()
                .startObject(ParsedMethodFields.METHOD_PARAMETER)
                .field("type", "keyword")
                .endObject()
                .startObject(ParsedMethodFields.JAR_NAME)
                .field("type", "keyword")
                .endObject()
                .startObject(ParsedMethodFields.TIME_STAMP)
                .field("type", "long")
                .endObject()
                .startObject(ParsedMethodFields.INVOKED_METHODS)
                .field("type", "text")
                .endObject()
                .endObject()
                .endObject();

        return mapping.string();
    }

    /**
     * Add Data to database if it is in particular format
     *
     * @param data Data to be added to Database
     * @return
     * @throws IOException
     */
    public boolean addData(Object data) throws IllegalArgumentException {
        if (data instanceof ArrayList) {
            ArrayList<JSONObject> jsonObjects = (ArrayList<JSONObject>) data;
            return addData(jsonObjects);
        } else if (data instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) data;
            return addData(jsonObject);
        } else {
            IllegalArgumentException exception = new IllegalArgumentException("Type of Object not supported");
            LOGGER.error(exception);
            throw exception;
        }
    }

    /**
     * Add single JSON object to ElasticSearch Database
     *
     * @param jsonObject Json Object
     */
    public boolean addData(JSONObject jsonObject) {
        try {
            XContentBuilder content = new XContentFactory().jsonBuilder();

            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String field = iterator.next();
                content.field(field, jsonObject.get(field));
            }

            buffer.add(new IndexRequest(indexname, type)
                    .source(content));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (buffer.size() >= MAX_BUFFER_SIZE)
            return sendDataFromBuffer();

        return true;
    }

    /**
     * Add multiple JSON object to ElasticSearchDatabase
     *
     * @param jsonObjects List of JSON objects
     */
    public boolean addData(List<JSONObject> jsonObjects) {
        try {
            for (JSONObject obj : jsonObjects) {
                XContentBuilder content = new XContentFactory().jsonBuilder()
                        .startObject();

                Iterator<String> it = obj.keys();
                while (it.hasNext()) {
                    String field = it.next();
                    content.field(field, obj.get(field));
                }
                content.endObject();

                buffer.add(new IndexRequest(indexname, type)
                        .source(content));
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (buffer.size() >= MAX_BUFFER_SIZE)
            return sendDataFromBuffer();

        return true;
    }

    /**
     * Returns all invoked methods inside method of a class
     * @param className
     * @param methodName
     * @return list of invoked methods
     */
    public List<String> getAllInvokedMethods(String className, String methodName, String methodParameters){
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryBuilder classNameMatchQuery = matchQuery(
                ParsedMethodFields.CLASS_NAME, className
        );

        QueryBuilder methodNameMatchQuery = matchQuery(
                ParsedMethodFields.METHOD_NAME, methodName
        );

        QueryBuilder methodParameterMatchQuery = matchQuery(
                ParsedMethodFields.METHOD_PARAMETER, methodParameters
        );

        BoolQueryBuilder query = QueryBuilders.boolQuery();
        query.must(classNameMatchQuery);
        query.must(methodNameMatchQuery);
        searchSourceBuilder.fetchSource(
                new String[]{
                        ParsedMethodFields.INVOKED_METHODS,
                }, null);
        searchSourceBuilder.query(query);
        searchSourceBuilder.size(1);
        searchSourceBuilder.sort(new FieldSortBuilder(ParsedMethodFields.TIME_STAMP).order(SortOrder.DESC));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse response = client.search(searchRequest);
            SearchHit matchedResult = response.getHits().getHits()[0];
            JSONObject result = new JSONObject(matchedResult.getSourceAsString());
            System.out.println(result.toString(2));
            JSONArray invokedMethods = (JSONArray) result.get(ParsedMethodFields.INVOKED_METHODS);
            ArrayList<String> listOfInvokedMethods = new ArrayList<String>();
            Iterator<Object> iterator = invokedMethods.iterator();
            while (iterator.hasNext()){
                listOfInvokedMethods.add((String) iterator.next());
            }
            return  listOfInvokedMethods;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        } catch (ArrayIndexOutOfBoundsException exception){
            LOGGER.error("No matches found", exception);
            return null;
        }
    }

    /**
     * Sends all data from buffer in a bulk request
     *
     * @return true if data was send properly
     */
    private boolean sendDataFromBuffer() {
        try {
            BulkRequest bulkRequest = new BulkRequest();

            while (!buffer.isEmpty())
                bulkRequest.add(buffer.remove());
            BulkResponse response = client.bulk(bulkRequest);
            if (response.status() == RestStatus.OK) {
                return true;
            } else {
                LOGGER.info(response.buildFailureMessage());
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return false;
    }

    /**
     * Check if index already exist in databaase
     *
     * @param index name of index
     * @return boolean true if index exist else false
     */
    private boolean isExistingIndex(String index) throws IOException {
        try {
            Response restResponse = client.getLowLevelClient().performRequest("GET", "/" + index);
            LOGGER.info(restResponse.getStatusLine());
            return true;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
        }

        return false;
    }

    /**
     * Close current connection with Elastic Search database
     */
    public void closeConnection() {
        if (!buffer.isEmpty()) {
            sendDataFromBuffer();
        }
        try {
            client.close();
            LOGGER.info("Database Connection Closed");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}