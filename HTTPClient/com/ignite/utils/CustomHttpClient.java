package com.ignite.utils;

import android.util.Log;

import com.aeioros.simplegob.surveys.appcore.R;
import com.aeioros.utils.ApplicationContextProvider;
import com.aeioros.utils.Constants;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.InputStreamBody;
//import org.apache.http.entity.mime.content.StringBody;

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
    private static final boolean HTTP_CONSOLE_ENABLED           = Constants.DEBUG;
    /** Set the default request type. If you dont specify in the call, this will be used. */
    private static final HttpMethod DEFAULT_CONNECTION_METHOD   = HttpMethod.GET;
    /** Define the time in milliseconds to cancel calls if server is not responding */
    private static final int CONNECTION_TIMEOUT                 = 10000;
    /** Provide a list of possible responses to validate them; Set to null if you dont want to check */
    private static final String[] VALID_RESPONSES               = {"true", "false", "0", "1", "2"};
    /** If your app will download JSONs, you can set a prefix to validate them. */
    public static final String JSON_PREFIX                      = "JSONResponse";

    /** Apache HttpClient used for establish connection with server */
    private static HttpClient httpClient;
    private static List<String> cookies;

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

    private static SSLContext getTrustfullSSLContext() {
        try {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
//            InputStream caInput = new BufferedInputStream(new FileInputStream("certificate.crt"));
            InputStream caInput = ApplicationContextProvider.getContext().getResources().openRawResource(R.raw.certificate_pro);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Execute a call to the given url with the default connection method.
     *
     * @param url to call as target of the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String execute(String url) throws Exception {
        return execute(url, new ArrayList<NameValuePair>(), DEFAULT_CONNECTION_METHOD, false);
    }

    /** Execute a call to the given url with the given params with the default connection method.
     *
     * @param url to call as target of the request.
     * @param params to send in the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String execute(String url, ArrayList<NameValuePair> params) throws Exception {
        return execute(url, params, DEFAULT_CONNECTION_METHOD, true);
    }

    /** Execute a call to the given url with the given params with the specified connection method.
     *
     * @param url to call as target of the request.
     * @param params to send in the request.
     * @param method that will be used for the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String execute(String url, ArrayList<NameValuePair> params, HttpMethod method) throws Exception {
        return execute(url, params, method, true);
    }

    /** Execute a call to the given url with the given params with the specified connection method.
     *
     * @param url to call as target of the request.
     * @param params to send in the request.
     * @param method that will be used for the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String execute(String url, ArrayList<NameValuePair> params, HttpMethod method, boolean validate) throws Exception {
        HttpsURLConnection urlConnection = null;
//        HttpURLConnection urlConnection = null;
        try {
            if (HTTP_CONSOLE_ENABLED) {
                Log.i("HTTP", url + "?" + paramsToString(params));
            }
            SSLContext context = getTrustfullSSLContext();
            URL requestedUrl = new URL(url + "?" + paramsToString(params));
            urlConnection = (HttpsURLConnection) requestedUrl.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
            if (method == HttpMethod.GET) {
                urlConnection.setRequestMethod("GET");
            } else if (method == HttpMethod.POST) {
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true); // Triggers POST.
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + "UTF-8");
            }
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(CONNECTION_TIMEOUT);

            if (url.contains(Constants.URLs.LOGIN)) {
                cookies = urlConnection.getHeaderFields().get("Set-Cookie");
            } else if (cookies != null) {
                for (String cookie : cookies) {
                    urlConnection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
            String result = readStream(urlConnection.getInputStream());
//            Log.i("HTTP", urlConnection.getHeaderField("set-cookie"));
            if (HTTP_CONSOLE_ENABLED) {
                Log.i("HTTP", result);
            }
            return result;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return "false";
    }

    public synchronized static String executeOld(String url, ArrayList<NameValuePair> params, HttpMethod method, boolean validate) throws Exception {
        BufferedReader in = null;
        try{
            if (HTTP_CONSOLE_ENABLED) {
                Log.i("HTTP", url);
            }
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
                Log.i("HTTP", result);
            }
            if (validate == false || (result != null && result.length() > 0 && isValidHttpResponse(result))) {
                return result;
            } else {
//                if (SyncAppHandler.isSynchronizing == false && SyncAppHandler.lastSynchronize < System.currentTimeMillis() - DateUtils.Times.MINUTE) {
//                    new SyncAppHandler(null).execute(SyncAppHandler.SynchronizeTaskType.SynchronizeTaskTypeLogin);
//                }
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



    private static String readStream(InputStream is) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            total.append(line);
        }
        if (reader != null) {
            reader.close();
        }
        return total.toString();
    }

    /** Execute a call to the given url with the given params with the specified connection method.
     *
     * @param url to call as target of the request.
     * @param params to send in the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static String executeMultipart(String url, ArrayList<NameValuePair> params, File file, boolean validate) throws Exception {
        BufferedReader in = null;
        return "false";
    }

    /** Execute a call to the given url with the given params with the specified connection method.
     *
     * @param url to call as target of the request.
     * @param params to send in the request.
     * @return the response text retrieved in the request.
     */
    public synchronized static File executeMultipart(String url, ArrayList<NameValuePair> params, File file) throws Exception {
        BufferedReader in = null;
        try{
            if (HTTP_CONSOLE_ENABLED) {
                Log.i("HTTP", url);
            }

            HttpClient client = getHttpClient();
            HttpResponse response = null;
            HttpGet request = new HttpGet();
            url += "?" + paramsToString(params);
            request.setURI(new URI(url));

            FileOutputStream fos = new FileOutputStream(file);
            /*if (file.exists()) {
                request.setHeader("Range", "bytes=" + file.length() + "-");
                fos = new FileOutputStream(file, true);
            } else {
                request.setHeader("Range", "bytes=" + 0 + "-");
                fos = new FileOutputStream(file);
            }*/

            //request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            response = client.execute(request);

            /*InputStream is = response.getEntity().getContent();

            int inByte;
            while((inByte = is.read()) != -1) {
                fos.write(inByte);
                if (HTTP_CONSOLE_ENABLED) {
                    Log.i("HTTP", "Downloading file ");
                }
            }
            is.close();*/

            response.getEntity().writeTo(fos);
            fos.close();

            return file;
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
        return null;
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

            if (replacedResponse.length() > JSON_PREFIX.length() && replacedResponse.substring(0, JSON_PREFIX.length()).equalsIgnoreCase(JSON_PREFIX)) return true;

            return false;
        } else {
            return true;
        }
    }
}
