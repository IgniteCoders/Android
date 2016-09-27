package com.ignite.HQLite.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.ignite.HQLite.PersistentEntity;
import com.ignite.HQLite.utils.ApplicationContextProvider;
import com.ignite.HQLite.utils.SQLConsole;

/**
 * Created by Mansour on 13/05/2016.
 */
public class DatabaseConnector {
    private Context context;
    protected SQLiteDatabase database;
    protected DatabaseManager databaseManager;
    private EntityManager entityManager;


    /**
     * Default constructor where you must pass the entity manager that will act as a data provider for this connector
     *
     * @param manager the entity manager that will use this connection
     * @see EntityManager
     */
    public DatabaseConnector(EntityManager manager) {
        entityManager = manager;
    }


    /**
     * Open a new database connection
     */
    public void open() throws SQLException {
        context = ApplicationContextProvider.getContext();
        databaseManager = new DatabaseManager(context);
        database = databaseManager.getReadableDatabase();
    }

    /**
     * Close the database connection
     */
    public void close() {
        databaseManager.close();
    }

    /**
     * Convenience method for persisting a object into the database.
     *
     * @param object the object to persist in the database
     * @throws SQLException
     * @return the row ID of the newly persisted object, or -1 if an error occurred
     */
    public long insert(PersistentEntity object) {
        SQLConsole.LogInsert(object);
        return database.insertOrThrow(entityManager.getTableName(), null, entityManager.getContentValues(object));
    }

    /**
     * Convenience method for updating a object already inserted into the database.
     *
     * @param object the object to update in the database
     * @throws SQLException
     * @return the number of rows affected
     */
    public int update(PersistentEntity object) {
        SQLConsole.LogUpdate(object);
        return database.update(entityManager.getTableName(), entityManager.getContentValues(object), DatabaseManager.ID_COLUMN_NAME + "=" + object.getId(), null);
    }


    /**
     * Convenience method for removing objects from the database.
     *
     * @param object the object to remove from the database
     * @return the number of rows affected, 1 if deleted correctly, 0 otherwise
     */
    public int delete(PersistentEntity object) {
        SQLConsole.LogDelete(object);
        return database.delete(entityManager.getTableName(), DatabaseManager.ID_COLUMN_NAME + "=" + object.getId(), null);
    }

    // Methods for retrieve cursors from database
    public Cursor get(long id) {
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, entityManager.getTableName(), entityManager.getColumnNames(), DatabaseManager.ID_COLUMN_NAME + "=" + id, null, null, null, null);
        SQLConsole.Log(sqlQry);
        return database.query(entityManager.getTableName(), entityManager.getColumnNames(), DatabaseManager.ID_COLUMN_NAME + "=" + id, null, null, null, null); //Utilizar El Id Android
    }

    public Cursor getByField(String key, Object value) {
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, entityManager.getTableName(), entityManager.getColumnNames(), key + "=" + value.toString(), null, null, null, null);
        SQLConsole.Log(sqlQry);
        return database.query(entityManager.getTableName(), entityManager.getColumnNames(), key + "='" + value.toString() + "'", null, null, null, null);
    }

    public Cursor getAll() {
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, entityManager.getTableName(), entityManager.getColumnNames(), null, null, null, null, null);
        SQLConsole.Log(sqlQry);
        return database.query(entityManager.getTableName(), entityManager.getColumnNames(), null, null, null, null, null);
    }

    public Cursor getAllByField(String key, Object value) {
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, entityManager.getTableName(), entityManager.getColumnNames(), key + "=" + value.toString(), null, null, null, null);
        SQLConsole.Log(sqlQry);
        return database.query(entityManager.getTableName(), entityManager.getColumnNames(), key + "=" + value.toString(), null, null, null, null);
    }

    public Cursor getAllByFieldInList(String key, Object[] values) {
        String inClause = "(";
        for(int i = 0; i < values.length; i++){
            inClause += values[i].toString();
            if(i != (values.length - 1)){
                inClause += ",";
            }
        }
        inClause += ")";
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, entityManager.getTableName(), entityManager.getColumnNames(), key + " in " + inClause, null, null, null, null);
        SQLConsole.Log(sqlQry);
        return database.query(entityManager.getTableName(), entityManager.getColumnNames(), key + " in " + inClause, null, null, null, null);
    }
}
