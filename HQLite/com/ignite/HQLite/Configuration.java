package com.ignite.HQLite;

/**
 * Created by Mansour on 30/09/2016.
 */

public class Configuration {
    /** Set to true if you need to print the SQL queries in the console */
    public static final boolean SQL_CONSOLE_ENABLED = true; // Constants.DEBUG;
    /** Defines the name of the identifier column in all tables */
    public static final String ID_COLUMN_NAME = "_id";
    /** The name of the database file that will be stored, it's must end with .db extension */
    public static final String DATABASE_NAME = "mydatabase.db";
    /** Number of the database version, should be increased on each database modification to recreate the database */
    public static final int DATABASE_VERSION = 1;
    /** Name of the package where the domain classes are placed, if set to "", HQLite will search in all the application */
    public static final String DOMAIN_PACKAGE = ""; // com.example.app.domain
    /** Name of the static property where you will store your EntityManagers of the domain classes */
    public static final String ACCESS_PROPERTY = "TABLE";
    /** Define database create/update behavior; one of 'create', 'drop-create', 'update', '', */
    public static final String DATABASE_CREATE = "drop-create";
    /** Set the default fetch behaviour, true for "LAZY" and false for "EAGER" */
    public static final boolean LAZY_FETCH_TYPE = false;
}
