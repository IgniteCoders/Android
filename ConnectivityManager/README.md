> After android 10.0 (API 29), dependencies used by this project becomes deprecated.

# ConnectivityManager
ConnectivityManager is an utility class with the purpose of checking if internet is reachable.

It's possible to check if there is an available connection and which type it's

## Installation
Just import the package `com.ignite.utils` under the folder ConnectivityManager to your project.

## Configuration
You need to declare this permissions:

```XML
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

## Usage
This class makes checks so easy as a simple code line:

`boolean connected = Connectivity.isAvailable() && Connectivity.isConnected();`

You can also check for a specific connection using:

 * `isConnectedWifi()`
 * `isConnectedMobile()`
 * `isConnectedFast()`

In the services package you can find a ReachabilityChangedService class, this class can be fired when connectivity changed,:

```Java
@Override
public void onReceive(Context context, Intent intent) {

    if (SessionManager.getSession().isLoggedIn() && Connectivity.isAvailable() && Connectivity.isConnected()) {
        Constants.Console.Log("ReachabilityChangedService", "isConnected");
        // Sync my app with the server.
        // It's highly recomended to use Firebase Job Dispatcher for backround tasks to prevent failures because of low resources right now.
        /*FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job myJob = dispatcher.newJobBuilder()
                .setService(SyncAppInBackgroundService.class) // the JobService that will be called
                .setTag("my-unique-tag")        // uniquely identifies the job
                .setConstraints(Constraint.ON_UNMETERED_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(myJob);*/
    }
}

```

If you need this last utility, be aware of adding the receiver to the Manifest like this:

```XML
<receiver
    android:name="com.ignite.utils.services.ReachabilityChangedService"
    android:enabled="true">
    <intent-filter>
        <category android:name="android.intent.category.DEFAULT" />

        <!-- Connectivity changed -->
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
        <!-- After poweron device -->
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.QUICKBOOT_POWERON" />
        <!-- For HTC devices -->
        <action android:name="com.htc.intent.action.QUICKBOOT_POWERON" />
    </intent-filter>
</receiver>
```
