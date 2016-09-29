package com.ignite.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

/**
 * Client used to interact with the network context by HTTP requests.
 *
 * CustomHttpClient can make calls via GET or POST with the given params and retrieve the resulting string.
 */
public class CustomHttpClient {

    /**
     * Enum to store the request types.
     */
    public enum HttpMethod {
        GET,
        POST,
    }

    /** Set to true if you need to print the URLs calls and responses in the console. */
    private static final boolean HTTP_CONSOLE_ENABLED           = true;
    /** Set the default request type. If you dont specify in the call, this will be used. */
    private static final HttpMethod DEFAULT_CONNECTION_METHOD   = HttpMethod.GET;
    /** Define the time in milliseconds to cancel calls if server is not responding */
    private static final int CONNECTION_TIMEOUT                 = 10000;
    /** Provide a list of possible responses to validate them; Set to null if you dont want to check */
    private static final String[] VALID_RESPONSES               = null; // Example: {"true", "false"}; 
    /** If your app will download JSONs, you can set a prefix to validate them. */
    public static final String JSON_PREFIX                      = "JSONResponse";

    /** Apache HttpClient used for establish connection with server */
    private static HttpClient httpClient;

    /** Return the HttpClient with the default configuration */
    private synchronized static HttpClient getHttpClient(){
        if(httpClient == null){
            httpClient = new DefaultHttpClient();
            final HttpParams params = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT);
            ConnManagerParams.setTimeout(params, CONNECTION_TIMEOUT);
        }
        return httpClient;
    }

    /** Execute a call to the given url with the default connection method.
     *
     * @param url to call as target of the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String execute(String url) throws Exception {
        return execute(url, new ArrayList<NameValuePair>());
    }

    /** Execute a call to the given url with the given params with the default connection method.
     *
     * @param url to call as target of the request.
     * @param params to send in the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String execute(String url, ArrayList<NameValuePair> params) throws Exception {
        return execute(url, params, DEFAULT_CONNECTION_METHOD);
    }

    /** Execute a call to the given url with the given params with the specified connection method.
     *
     * @param url to call as target of the request.
     * @param params to send in the request.
     * @param method that will be used for the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String execute(String url, ArrayList<NameValuePair> params, HttpMethod method) throws Exception {
        BufferedReader in = null;
        try{
            HttpClient client = getHttpClient();
            HttpResponse response = null;
            if (method == HttpMethod.GET) {
                HttpGet request = new HttpGet();
                url += "?" + paramsToString(params);
                request.setURI(new URI(url));
                response = client.execute(request);
            } else if (method == HttpMethod.POST) {
                HttpPost request = new HttpPost(url);
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params,"UTF-8");
                formEntity.setContentEncoding(HTTP.UTF_8);
                request.setEntity(formEntity);
                request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                response = client.execute(request);
            }

            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String lineSeparator = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + lineSeparator);
            }
            String result = sb.toString();

            if (HTTP_CONSOLE_ENABLED) {
                Log.i("HTTP", url);
                Log.i("HTTP", result);
            }
            if (result != null && result.length() > 0 && isValidHttpResponse(result)) {
                return result;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "false";
    }

    /** Method for encode a string with params as URL specification for "GET" connection method.
     *
     * @param params to encode in the string.
     * @return the string containing the params encoded with UTF-8.
     */
    public static String paramsToString(ArrayList<NameValuePair> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (NameValuePair param : params) {
            sb.append("");
            sb.append(param.getName());
            sb.append("=");
            if (param.getValue() != null) {
                String value = URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8");
                sb.append(value);
            } else {
                sb.append("null");
            }
            sb.append("&");
        }
        return sb.toString();
    }

    /** Method to check if a response is valid to handle in the app or is just an error in server.
     *
     * @param response to check if is valid.
     * @return true if pass all checks successfully, false if not.
     */
    public static boolean isValidHttpResponse(String response) {
        if (VALID_RESPONSES != null) {
            String replacedResponse = response.replaceAll("\\n", "");    // Newline
            replacedResponse = replacedResponse.replaceAll("\\{", "");    // {
            replacedResponse = replacedResponse.replaceAll("\\}", "");    // }
            replacedResponse = replacedResponse.replaceAll(" ", "");    // White spaces
            replacedResponse = replacedResponse.replaceAll("\"", "");    // Double cuotes

            for (String validResponse : VALID_RESPONSES) {
                if (replacedResponse.equalsIgnoreCase(validResponse)) return true;
            }

            if (replacedResponse.substring(0, JSON_PREFIX.length()).equalsIgnoreCase(JSON_PREFIX)) return true;

            return false;
        } else {
            return true;
        }
    }
}
