package com.shravan.project.movingcars.util;

import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Helper class providing common util methods for serialization,
 * deserialization, url query fetch etc
 * 
 * @author shravanshetty
 */
public class ServerUtils {

    private static final ObjectMapper om = new ObjectMapper();
    private static final Logger log = Logger.getLogger(ServerUtils.class.getSimpleName());

    /**
     * Serializes the given object instance
     * 
     * @param objectToBeSerialized
     * @return
     */
    public static String serialize(Object objectToBeSerialized) {

        if (objectToBeSerialized == null) {
            return null;
        }
        ObjectWriter ow = om.writer();
        try {
            return ow.writeValueAsString(objectToBeSerialized);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the instance of the ObjectMapper
     * 
     * @return
     */
    public static ObjectMapper getMapper() {

        return om;
    }

    /**
     * Deserialize a json string to a given Type
     * 
     * @param jsonString
     * @param throwException
     *            When set to false: Silently tries to deserialize. Logs the
     *            exception, doesnt throw it, returns null.
     * @param type
     * @return
     * @throws Exception
     */
    public static <T> T deserialize(String jsonString, boolean throwException, TypeReference<T> type) throws Exception {

        try {
            return om.readValue(jsonString, type);
        }
        catch (Exception e) {
            if (throwException) {
                throw e;
            }
            else {
                log.severe(e.getLocalizedMessage());
                return null;
            }
        }
    }

    /**
     * Deserialize a json string to a given Type
     * 
     * @param jsonString
     * @param type
     * @return
     * @throws Exception
     */
    public static <T> T deserialize(String jsonString, Boolean throwException, Class<T> type) throws Exception {

        try {
            return om.readValue(jsonString, type);
        }
        catch (Exception e) {
            if (throwException) {
                throw e;
            }
            else {
                log.severe(e.getLocalizedMessage());
                return null;
            }
        }
    }

    /**
     * Converts the given jsonObject to the required. Throws an exception if
     * throwException is set to true
     * 
     * @param jsonObject
     * @param throwException
     * @param type
     * @return
     * @throws Exception
     */
    public static <T> T convert(Object jsonObject, boolean throwException, TypeReference<T> type) throws Exception {

        try {
            return om.convertValue(jsonObject, type);
        }
        catch (Exception e) {
            if (throwException) {
                throw e;
            }
            else {
                log.severe(e.getLocalizedMessage());
                return null;
            }
        }
    }
    
    /**
     * Returns all the query parameters in the url given.
     * @return
     * @throws Exception
     */
    public static HashMap<String, String> getAllQuerParameters(String url) throws Exception {

        url = url.replace(" ", URLEncoder.encode(" ", "UTF-8"));
        URIBuilder uriBuilder = new URIBuilder(new URI(url));
        HashMap<String, String> result = new HashMap<String, String>();
        for (NameValuePair nameValue : uriBuilder.getQueryParams()) {
            result.put(nameValue.getName(), nameValue.getValue());
        }
        return result;
    }
}
