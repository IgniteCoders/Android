package com.ignite.HQLite.utils;

import android.util.Log;

import com.ignite.HQLite.Configuration;
import com.ignite.HQLite.PersistentEntity;

import java.lang.reflect.Field;

/**
 * Created by Mansour on 13/05/2016.
 */
public class SQLConsole {
    private static final String TAG = "DatabaseManager";

    public static void Log(String message) {
        if (Configuration.SQL_CONSOLE_ENABLED) {
            Log.i(TAG, message);
        }
    }

    public static void LogInsert(PersistentEntity insertedObject) {
        if (Configuration.SQL_CONSOLE_ENABLED) {
            try {
                String sqlQry = "INSERT INTO " + insertedObject.getTableData().getTableName() + " (_id";
                String sqlValues = "VALUES (" + insertedObject.getId();
                for (int i = 0; i < insertedObject.getTableData().getColumnFields().size(); i++) {
                    Field field = (Field) insertedObject.getTableData().getColumnFields().get(i);
                    if (!EntityFieldHelper.isCollectionRelationField(field)) {
                        String columnName = SQLQueryGenerator.getColumnName(field);
                        sqlQry += ", " + columnName;
                        sqlValues += ", " + field.get(insertedObject);
                    }
                }
                sqlQry += ") " + sqlValues + ")";
                Log.i(TAG, sqlQry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void LogUpdate(PersistentEntity updatedObject) {
        if (Configuration.SQL_CONSOLE_ENABLED) {
            try {
                String sqlQry = "UPDATE " + updatedObject.getTableData().getTableName() + " SET ";
                for (int i = 0; i < updatedObject.getTableData().getColumnFields().size(); i ++) {
                    Field field = (Field) updatedObject.getTableData().getColumnFields().get(i);
                    if (!EntityFieldHelper.isCollectionRelationField(field)) {
                        String columnName = SQLQueryGenerator.getColumnName(field);
                        sqlQry += ", " + columnName + " = " + field.get(updatedObject);
                    }
                }
                sqlQry += " WHERE " + Configuration.ID_COLUMN_NAME + "=" + updatedObject.getId();
                Log.i(TAG, sqlQry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void LogDelete(PersistentEntity deletedObject) {
        if (Configuration.SQL_CONSOLE_ENABLED) {
            try {
                String sqlQry = "DELETE FROM " + deletedObject.getTableData().getTableName() + " WHERE " + Configuration.ID_COLUMN_NAME + "=" + deletedObject.getId();
                Log.i(TAG, sqlQry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
