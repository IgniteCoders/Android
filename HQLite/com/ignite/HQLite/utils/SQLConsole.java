package com.ignite.HQLite.utils;

import android.util.Log;

import com.ignite.HQLite.Configuration;
import com.ignite.HQLite.PersistentEntity;

import java.lang.reflect.Field;
import java.util.Map;

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
                String sqlQry = "INSERT INTO " + insertedObject.getTableData().getTableName() + " (" + Configuration.ID_COLUMN_NAME;
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

    public static void LogUpdate(String tableName, CriteriaBuilder criteria, Map<String, Object> values) {
        if (Configuration.SQL_CONSOLE_ENABLED) {
            try {
                String sqlQry = "UPDATE " + tableName + " SET ";
                String[] flieds = values.keySet().toArray(new String[values.keySet().size()]);
                for (int i = 0; i < flieds.length; i ++) {
                    String fieldName = flieds[i];
                    sqlQry += ", " + fieldName + " = " + values.get(fieldName);
                }
                sqlQry += " WHERE " + criteria.query();
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

    public static void LogDelete(String tableName, CriteriaBuilder criteria) {
        if (Configuration.SQL_CONSOLE_ENABLED) {
            try {
                String sqlQry = "DELETE FROM " + tableName + " WHERE " + criteria.query();
                Log.i(TAG, sqlQry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
