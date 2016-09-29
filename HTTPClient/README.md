# HTTPClient
HTTPClient is an utility class done to can make and handle HTTP requests.

It's possible make call to a given URL with its parameters and retrieve the response text.

## Installation
Just import the class `CustomHttpClient` under the folder com.ignite.utils to your project.

## Configuration
After android 6.0 (API 23), apache http client library is not longer provided and you need to add dependency manually.

Go to your build.gralde an put inside dependencies the next line:

`compile 'org.jbundle.util.osgi.wrapped:org.jbundle.util.osgi.wrapped.org.apache.http.client:4.1.2'`

HTTPClient dont needs configuration to makes it work, but you can set the next settings:

```Java
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

```

## Usage
This class makes requests so easy as a simple code line:

`String response = new CustomHttpClient().execute("www.example.com");`

You can also specify the connection method and provide parameters using:

 * `org.apache.http.NameValuePair;`
 * `org.apache.http.message.BasicNameValuePair;`

Here is a practical example:

```Java
try {
    String serverUrl = "www.example.com/authenticate";
    ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
    parameters.add(new BasicNameValuePair("username", username));
    parameters.add(new BasicNameValuePair("password", password));

    String response = new CustomHttpClient().execute(serverUrl, parameters, CustomHttpClient.HttpMethod.POST);

    publishProgress(Long.valueOf(10));
    if(response.equals("true")){
        return 1; // Login success
    } else if ( response.equals("false")) {
        return 0; // Wrong credentials
    } else {
        return -1; // Error
    }
} catch (Exception e) {
    e.printStackTrace();
    return -1; // Connection error, timeout...
}

```
