package com.ignite.HQLite.managers;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ignite.HQLite.PersistentEntity;
import com.ignite.HQLite.utils.Reflections;
import com.ignite.HQLite.utils.SQLConsole;
import com.ignite.networker.utils.Constants;

import java.io.IOException;


/**
 * Class that manages the database create and upgrade, connections and defines some configuration
 * @author Mansour
 *
 */

public class DatabaseManager extends SQLiteOpenHelper {

    /** Set to true if you need to print the SQL queries in the console */
    public static final boolean SQL_CONSOLE_ENABLED = true;
    /** Defines the name of the identifier column in all tables */
    public static final String ID_COLUMN_NAME = "_id";
    /** The name of the database file that will be stored, it's must end with .db extension */
	private static final String DATABASE_NAME = "mydatabase.db";
    /** Number of the database version, should be increased on each database modification to recreate the database */
    private static final int DATABASE_VERSION = 1;
    /** Name of the package where the domain classes are placed, if set to "", HQLite will search in all the application */
    private static final String DOMAIN_PACKAGE = "";


    /**
     * Default constructor to create a manager for database connection
     *
     * @param context the application context
     * @see DatabaseConnector
     */
	public DatabaseManager(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase database) {
        SQLConsole.Log("Creating database file with name " + DATABASE_NAME + " and version " + DATABASE_VERSION);
    	try{
			Class[] classes = Reflections.getSubClassesOfClass(PersistentEntity.class, DOMAIN_PACKAGE);
			for (Class<PersistentEntity> currentClass : classes) {
                SQLConsole.Log(SQLQueryGenerator.getCreateTable(currentClass));
                database.execSQL(SQLQueryGenerator.getCreateTable(currentClass));
			}
    	} catch(SQLException e){
    		e.printStackTrace();
    	} catch (IOException e) {
			e.printStackTrace();
		}
	}
        
    @Override
    public void onUpgrade(SQLiteDatabase database,  int oldVersion, int newVersion) {
        SQLConsole.Log("Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

        try{
            Class[] classes = Reflections.getSubClassesOfClass(PersistentEntity.class, DOMAIN_PACKAGE);
            for (Class<PersistentEntity> currentClass : classes) {
                SQLConsole.Log(SQLQueryGenerator.getDropTable(currentClass));
                database.execSQL(SQLQueryGenerator.getDropTable(currentClass));
            }
        } catch(SQLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    	onCreate(database);
    }
    
    
    public void clearDataBase () {
    	SQLiteDatabase db = getWritableDatabase();
    	onUpgrade(db, 0, DATABASE_VERSION);
    	db.close();
    }
}
    
