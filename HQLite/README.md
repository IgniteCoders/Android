# HQLite
HQLite is an object-relational mapping framework for the Android platform based on SQLite database.

It provides a framework for mapping an object-oriented domain model to a relational database (SQLite). HQLite improves database access and code by replacing DAOs with automatized database creation, connection... and easy object handling functions.

HQLite is a free library that is still under development and distributed under the GNU General Public License 3.0.

HQLite's primary feature is using reflection for mapping from Java classes to database tables; and mapping from Java data types to SQL data types. Whitch also provides data query and retrieval facilities, generating SQL calls and relieves the developer from manual handling and object conversion of the result set.

 * [Installation](https://github.com/IgniteCoders/HQLite#installation)
 * [Configuration](https://github.com/IgniteCoders/HQLite#configuration)
 * [Usage](https://github.com/IgniteCoders/HQLite#usage)
 	- [1 Quick Start Guide](https://github.com/IgniteCoders/HQLite#1-quick-start-guide)
 	- [2 Domain modelling](https://github.com/IgniteCoders/HQLite#2-domain-modelling)
 		- [2.1 Creating a domain class](https://github.com/IgniteCoders/HQLite#21-creating-a-domain-class)
 		- [2.2 Defining properties](https://github.com/IgniteCoders/HQLite#22-defining-properties)
 		- [2.3 Association](https://github.com/IgniteCoders/HQLite#23-association)
 			- [2.3.1 Many-to-one and one-to-one](https://github.com/IgniteCoders/HQLite#231-many-to-one-and-one-to-one)
 			- [2.3.2 One-to-many](https://github.com/IgniteCoders/HQLite#232-one-to-many)
 			- [2.3.3 Many-to-many](https://github.com/IgniteCoders/HQLite#233-many-to-many)
 			- [2.3.4 In conclusion](https://github.com/IgniteCoders/HQLite#234-in-conclusion)
 	- [3 Inheritance](https://github.com/IgniteCoders/HQLite#3-inheritance)
 	- [4 Basic CRUD](https://github.com/IgniteCoders/HQLite#4-basic-crud)
 	- [5 Constraints](https://github.com/IgniteCoders/HQLite#5-constraints)
 	- [6 Events](https://github.com/IgniteCoders/HQLite#6-events)
 	- [7 Server ids and relationships](https://github.com/IgniteCoders/HQLite#7-server-ids-and-relationships)
 	- [8 JSON wraper/parser](https://github.com/IgniteCoders/HQLite#8-json-wraperparser)


## Installation
Just import the folder com.ignite.HQLite to your project.

## Configuration
HQLite needs a little Configuration-over-Figuration to make it work:

  * SQLite needs access to the Android Context.
    - The easiest way for that, is placing in the manifest the attribute name of the application tag, pointing to the `com.ignite.HQLite.utils.ApplicationContextProvider` class as below:
    
    
    ```XML
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
  
    Navigate through the HQlite library folders and find the `com.ignite.HQLite.managers.DatabaseManager` class. In this class you need to set two properties:
    - The database name.
    - The database version.
  ```Java
	private static final String DATABASE_NAME = "mydatabase.db";
	private static final int DATABASE_VERSION = 1;
  ```




  * Define some optional settings:
  
    Now HQLite can be used but lets stay in the `DatabaseManager` to set some more properties:
    
    - The package name where you will place your domain classes.
    
    	This is used to enhance the search of your classes, if you set it to "", HQLite will search in all the application for every class thats extends `com.ignite.HQLite.PersistentEntity`.
	
    - The SQL Console.
    
    	Set to true if you want to print the queries in the console.
  ```Java
	private static final String DOMAIN_PACKAGE = "";
	public static final boolean SQL_CONSOLE_ENABLED = true;
  ```



## Usage

### 1 Quick Start Guide
Domain classes are core to any business application. They hold state about business processes and hopefully also implement behavior. They are linked together through relationships; one-to-one, one-to-many, or many-to-many.

HQLite implements object relational mapping (ORM). Under the hood it uses [SQLite](https://www.sqlite.org/about.html) (a very popular and flexible [public domain library](https://www.sqlite.org/copyright.html)) and thanks to the dynamic reflection of Java, there is far less configuration involved in creating HQLite domain classes and operate with them.

### 2 Domain modelling
When building Android applications you have to consider the problem domain you are trying to solve. For example if you were building an Amazon-style bookstore you would be thinking about books, authors, customers and publishers to name a few.
These are modeled in HQLite classes, so a `Book` class may have a title, a release date, an ISBN number and so on. The next few sections show how to model the domain in HQLite.

### 2.1 Creating a domain class
To create a domain class you need to extends `com.ignite.HQLite.PersistentEntity` and declare a static field `TABLE` with its `com.ignite.HQLite.managers.EntityManager` as follows:

  ```Java
	public class Book extends PersistentEntity {
		public static final EntityManager<Book> TABLE = new EntityManager<Book>(){};
	}
  ```
This class will map automatically to a table in the database called book (the same name as the class).

### 2.2 Defining properties
Now that you have a domain class you can define its properties as Java types. For example:

  ```Java
	public class Book extends PersistentEntity {
		public static final EntityManager<Book> TABLE = new EntityManager<Book>(){};
		public String title;
		public long releaseDate;
		public String ISBN;
	}
  ```
Each property is mapped to a column in the database, where the convention for column names is like Java. For example releaseDate maps onto a column releaseDate. The SQL types are auto-detected from the Java types, but only this types are handled:

Java type | SQLite type
--- | ---
`String` | `TEXT`
`long` | `LONG`
`double` | `DOUBLE`
`int` | `INTEGER`
`boolean` | `BOOLEAN`
`byte` | `BLOB`
`Date` | `LONG`*

	* If you need to store a Date, is expected to be a `long` field, and transform it on getter/setter method

### 2.3 Association
Relationships define how domain classes interact with each other. Unless specified explicitly at both ends, a relationship exists only in the direction it is defined.

### 2.3.1 Many-to-one and one-to-one
A Many-to-one relationship is the simplest kind, and is defined with a property of the type of another domain class. Consider this example:

**Example A**

  ```Java
	public class Face extends PersistentEntity {
		public static final EntityManager<Face> TABLE = new EntityManager<Face>(){};
		public Nose nose;
	}
	public class Nose extends PersistentEntity {
		public static final EntityManager<Nose> TABLE = new EntityManager<Nose>(){};
	}
  ```
In this case we have a unidirectional many-to-one relationship from `Face` to `Nose`. To make this relationship bidirectional define the both sides as follows:

**Example B**

  ```Java
	public class Face extends PersistentEntity {
		...
		@HasOne(mappedBy = "face")
		public Nose nose;
	}
	public class Nose extends PersistentEntity {
		...
		@BelongsTo(mappedBy = "nose")
		public Face face;
	}
  ```
In this case we use the `BelongsTo` and `HasOne` annotations to say that `Nose` "belongs to" `Face`. While in the example A, a `Nose` "can be used by many" Faces because the foreign-key is placed in the table `Face`, in the example B the foreign-key is placed in the table Nose.

This also affect the fetch type. If we annotate a field with `BelongsTo`, HQLite will use lazy fetch type to retrieve an empry object with its id. While in the other cases will use eager fetch type.

The result of this is that we can create a `Face`, attach a Nose instance to it and when we save or delete the `Face` instance, HQLite will save or delete the `Nose`. In other words, saves and deletes will cascade from `Face` to the associated `Nose`:
  ```Java
  Face f = new Face();
  f.nose = new Nose();
  f.save();
  ```

Note that using this annotations puts the foreign-key on the inverse table to the example A, so in this case the foreign-key column is stored in the Nose table inside a column called "idFace". Also, `HasOne` only works with bidirectional relationships.

### 2.3.2 One-to-many
A one-to-many relationship is when one class, example `Author`, has many instances of another class, example `Book`. With HQLite you define such a relationship with the `HasMany` annotation instead of `HasOne`:

**Example C**

  ```Java
	public class Author extends PersistentEntity {
		...
		@HasMany(mappedBy = "author")
		public List<Book> books;
		public String name;
	}
	public class Book extends PersistentEntity {
		...
		@BelongsTo(mappedBy = "books")
		public Author author;
		public String title;
	}
  ```
  
Remember that like `HasOne` annotation, `HasMany` will cause cascade saves and deletes in collections, with its correspondant `BelongsTo` in the other side of the relation.

If we have more than one property of the same type, attribute `mappedBy` will be used to specify which collection is mapped:

**Example D**
  ```Java
	public class Airport extends PersistentEntity {
		...
		@HasMany(mappedBy = "departureAirport")
		public List<Flight> flights;
	}
	public class Flight extends PersistentEntity {
		...
		@BelongsTo(mappedBy = "flights")
		Airport departureAirport;
		Airport destinationAirport;
	}
  ```

**Example E**
  ```Java
	public class Airport extends PersistentEntity {
		...
		@HasMany(mappedBy = "departureAirport")
		public List<Flight> outboundFlights;
		@HasMany(mappedBy = "destinationAirport")
		public List<Flight> inboundFlights;
	}
	public class Flight extends PersistentEntity {
		...
		@BelongsTo(mappedBy = "outboundFlights")
		Airport departureAirport;
		@BelongsTo(mappedBy = "inboundFlights")
		Airport destinationAirport;
	}
  ```
### 2.3.3 Many-to-many
HQLite doesn't supports many-to-many relationships, but here is an example of how to implement your own table for that. Suppose that you want the next model:

**Example F**

  ```Java
	public class Author extends PersistentEntity {
		...
		@HasMany(mappedBy = "authors")
		public List<Book> books;
		public String name;
	}
	public class Book extends PersistentEntity {
		...
		@HasMany(mappedBy = "books")
		public List<Author> authors;
		public String title;
	}
  ```
To implement this relationship in HQLite, you need to create a new domain class, for example: `AuthorBook`. The resulting model will be the follow:

**Example G**

  ```Java
	public class Author extends PersistentEntity {
		...
		@HasMany(mappedBy = "author")
		public List<AuthorBook> books;
		public String name;
	}
	public class Book extends PersistentEntity {
		...
		@HasMany(mappedBy = "book")
		public List<AuthorBook> authors;
		public String title;
	}
	public class AuthorBook extends PersistentEntity {
		...
		@BelongTo(mappedBy = "books")
		public Author author;
		@BelongTo(mappedBy = "authors")
		public Book book;
	}
  ```
  
### 2.3.4 In conclusion
HQLite supports/expects the following relationships:

Relationship type | Example of use | Description | Cascade Behaviour
--- | :---: | --- | --- | ---
Many-to-one | A | `Face` use `Nose`. | NO
One-to-one | B | `Face` has one `Nose`. | YES
One-to-many | C | `Author` has many `Books` | YES
Many-to-many | G | `Author` has many `Books` and `Book` has many `Authors` | YES

### 3 Inheritance
HQLite supports inheritance so simple as extending another domain class

```Java
	public class Content extends PersistentEntity {
		public static final EntityManager<Content> TABLE = new EntityManager<Content>(){};
		
		@BelongsTo(mappedBy = "books")
		public Author author;
		public String title;
	}
	
	public class BlogEntry extends Content {
		public static final EntityManager<BlogEntry> TABLE = new EntityManager<BlogEntry>(){};
		
		URL url
	}
	public class Book extends Content {
		public static final EntityManager<Book> TABLE = new EntityManager<Book>(){};
		
		String ISBN
	}
	public class PodCast extends Content {
		public static final EntityManager<PodCast> TABLE = new EntityManager<PodCast>(){};
		
		byte[] audioStream
	}
```
In the above example we have a parent `Content` class and then various child classes with more specific behaviour.

**Considerations**

HQLite implements inheritance using one table for each subclass. However, excessive use of inheritance and table-per-subclass can result in poor query performance due to the use of many queries for each row to fetch all fields in all subclasses. In general our advice is if you're going to use inheritance, don't abuse it and don't make your inheritance hierarchy too deep.

**Polymorphic Queries**

The upshot of inheritance is that you get the ability to polymorphically query. For example using the `getAll()` method on the `Content` super class will return all subclasses of `Content`:
```Java
List<Content> contents = Content.TABLE.getAll(); // list all blog entries, books and podcasts
contents = Content.TABLE.getAllByField("author", "Joe Bloggs"); // find all by author
contents = PodCast.TABLE.getAll(); // list only podcasts
List<PodCast> podCasts = PodCast.TABLE.getAll(); // list only podcasts
```

### 4 Basic CRUD
Try performing some basic CRUD (Create/Read/Update/Delete) operations.

**Create**

To create an instance of a domain class, all what you need is to instantiate it and call method `insert()`:
```Java
Person p = new Person();
p.name = "Fred";
p.age = 40;
p.insert();
```

**Read**

HQLite transparently adds an implicit id property to your domain class which you can use for retrieval:
```Java
Person p = Person.TABLE.get(1);
```

**Update**

To update an instance, change some properties and then call `update()` again:
```Java
Person p = Person.TABLE.get(1);
p.name = "Bob";
p.update();
```

**Delete**

To delete an instance use the `delete()` method:
```Java
Person p = Person.TABLE.get(1);
p.delete();
```

**Save**

If you don't care about if insert or update, use `save()` method whitch will check if the instance has been already inserted (id is not null and row with that id is not null too), to perform an update or insert:

```Java
Person p = new Person();
p.save(); // Will call insert
p = Person.TABLE.get(1);
p.save(); // Will call update
```

### 5 Constraints
Within a domain class constraints are defined with the `Constraints` annotation:

```Java
public class User extends PersistentEntity {
	    public static final EntityManager<User> TABLE = new EntityManager<User>(){};

	    @Constraints(unique = true, nullable = false)
	    public String username = null;
	    public String password = null;
}
```

### 6 Events
HQLite supports the registration of events as methods that get fired when certain events occurs such as deletes, inserts and updates. The following is a list of supported events:

 * `beforeInsert` - Executed before an object is initially persisted to the database. If you return false, the insert will be cancelled.
 * `beforeUpdate` - Executed before an object is updated. If you return false, the update will be cancelled.
 * `beforeDelete` - Executed before an object is deleted. If you return false, the delete will be cancelled.
 * `afterInsert` - Executed after an object is persisted to the database
 * `afterUpdate` - Executed after an object has been updated
 * `afterDelete` - Executed after an object has been deleted

To add an event simply override the relevant method with your domain class. Remember to call super to perform the needed actions in each case.

```Java
public class Person extends PersistentEntity {
	public static final EntityManager<Person> TABLE = new EntityManager<Person>(){};
	    
	public String firstName;
	public String lastName;
	public long signupDate;

	@Override
	private boolean beforeInsert() {
		super.beforeInsert(); // HQLite use this methods to perform cascades. Remind to call super
		signupDate = System.currentTimeMillis();
		return true; // If everything goes well, return true to continue with insert or false to cancell it.
	}
}
```

### 7 Server ids and relationships
HQLite transparently adds an implicit id property to your domain class which you can use for retrieval. This id is used by default to retrieve the relationship objects. But if your app needs to clone/download a server database in local, you need to specify whitch property define the relationship in the server to mantain the correspondent object with their owners.

All what you need to do, is adding a property named id + name of the domain class and overriding serverId getter/setter/getBy as below:

```Java
public class User extends PersistentEntity {
    public static final EntityManager<User> TABLE = new EntityManager<User>(){
        @Override
        public User getByServerId(long id) {
            return getByField("idUser", id);
        }
    };
    public long idUser;
    @Constraints(unique = true, nullable = false)
    public String username = null;
    public String password = null;
    @HasMany(mappedBy = "user")
    public List<Account> accounts;
	    
    @Override
    public Long getServerId() {
        return idUser;
    }

    @Override
    public void setServerId(Long id) {
        this.idUser = id;
    }
}

public class Account extends PersistentEntity {
    public static final EntityManager<Account> TABLE = new EntityManager<Account>(){
        @Override
        public Account getByServerId(long id) {
            return getByField("idAccount", id);
        }
    };
    public long idAccount;
    public String number;
    @BelongsTo(mappedBy = "accounts")
    public User user;
	    
    @Override
    public Long getServerId() {
        return idAccount;
    }

    @Override
    public void setServerId(Long id) {
        this.idAccount = id;
    }
}
```
What we want to get here? When you download users from a remote database, that database already have their own ids. So, the column id in the remote database will define the relationship with the other tables. Imagine the following case:

**Server database:**

 * Table User

id | username | password
--- | --- | ---
2 | user1 | 123
5 | user2 | 456

 * Table Account:

id | idUser | number
--- | --- | ---
18 | 2 | 0001
19 | 2 | 0002
21 | 5 | 0004

**Local database**

 * Table User:

id | idUser | username | password
--- | --- | --- | ---
1 | 2 | user1 | 123
2 | 5 | user2 | 456

 * Table Account:

id | idAccount | idUser | number
--- | --- | --- | ---
1 | 18 | 2 | 0001
2 | 19 | 2 | 0002
3 | 21 | 5 | 0004

If we dont add the server field and override the serverId getter/setter and getByServerId(), HQLite will retrieve the user with the wrong accounts (Not related as in server).

**Example:**
```Java
User u1 = User.TABLE.get(1); // Will retrieve an empty account list, but this user has 2 accounts in server
User u2 = User.TABLE.get(2); // Will retrieve 2 accounts, but this user has only one account in server
```

### 8 JSON wraper/parser

HQLite implements `JSON` conversion for single instances or lists as the example below:

```Java
JSONObject oJSONData = ...;
User user = User.TABLE.parse(oJSONData);
oJSONData = User.TABLE.wrap(user);

JSONArray aJSONData = ...;
List<User> users = User.TABLE.parse(aJSONData);
aJSONData = User.TABLE.wrap(users);
```
If the `JSON` object contains a list of accounts, this will be parsed/wrapped too.
