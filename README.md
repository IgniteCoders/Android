# HQLite
HQLite is an object-relational mapping framework for the Android platform based on SQLite database.

It provides a framework for mapping an object-oriented domain model to a relational database (SQLite). HQLite improves database access and code by replacing DAOs with automatized database creation, connection... and easy object handling functions.

HQLite is a free library that is still under development and distributed under the GNU General Public License 3.0.

HQLite's primary feature is using reflection for mapping from Java classes to database tables; and mapping from Java data types to SQL data types. Whitch also provides data query and retrieval facilities, generating SQL calls and relieves the developer from manual handling and object conversion of the result set.


## Configuration
HQLite needs a little Configuration-over-Figuration to make it work:

  * SQLite needs access to the Android Context.
    - The easiest way for that, is placing in the manifest the attribute name of the application tag, pointing to the `com.ignite.HQLite.utils.ApplicationContextProvider` class as below:
    
    ```
    <application
        android:name="com.ignite.HQLite.utils.ApplicationContextProvider"
        android:label="@string/app_name"
        ... >
        <activity ... >
        </activity>
    </application>
    ```
    - If you already use that attribute for your own class, you need to decide if `ApplicationContextProvider` extends your class or vice versa, and implement it.
    
  * Define some setting of the database file:
  
    Navigate through the HQlite library folders and find the `com.ignite.HQLite.managers.DatabaseManager` class. In this class you need to set two propperties:
    - The database name:
    - The database version:
  ```
	private static final String DATABASE_NAME = "mydatabase.db";
	private static final int DATABASE_VERSION = 1;
  ```
