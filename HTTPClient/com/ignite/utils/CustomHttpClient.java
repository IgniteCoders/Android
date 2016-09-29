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

public class CustomHttpClient {
    public enum HttpMethod {
        GET,
        POST,
    }

    private static final boolean HTTP_CONSOLE_ENABLED           = true;
    public static final String JSON_PREFIX                     = "JSONResponse";
    private static final HttpMethod DEFAULT_CONNECTION_METHOD   = HttpMethod.GET;
    private static final int CONNECTION_TIMEOUT                 = 10000;

    private static HttpClient httpClient;

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

    public synchronized static String execute(String url, ArrayList<NameValuePair> params) throws Exception {
        return execute(url, params, DEFAULT_CONNECTION_METHOD);
    }

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
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
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

    public static boolean isValidHttpResponse(String response) {
        String replacedResponse = response.replaceAll("\\n", ""); 	// Elimino \n
        replacedResponse = replacedResponse.replaceAll("\\{", "");	// Elimino {
        replacedResponse = replacedResponse.replaceAll("\\}", "");	// Elimino {
        replacedResponse = replacedResponse.replaceAll(" ", "");	// Elimino Espacios en Blanco
        replacedResponse = replacedResponse.replaceAll("\"", "");	// Elimino "

        if (replacedResponse.equalsIgnoreCase("true")) return true;
        if (replacedResponse.equalsIgnoreCase("false")) return true;
        if (replacedResponse.equalsIgnoreCase("0")) return true;
        if (replacedResponse.equalsIgnoreCase("1")) return true;
        if (replacedResponse.equalsIgnoreCase("2")) return true;
        if (replacedResponse.substring(0, JSON_PREFIX.length()).equalsIgnoreCase(JSON_PREFIX)) return true;

        return false;
    }
}
