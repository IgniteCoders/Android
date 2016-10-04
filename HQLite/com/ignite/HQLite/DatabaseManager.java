package com.ignite.HQLite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ignite.HQLite.annotations.HasOne;
import com.ignite.HQLite.utils.EntityFieldHelper;
import com.ignite.HQLite.utils.Reflections;
import com.ignite.HQLite.utils.SQLConsole;
import com.ignite.HQLite.utils.SQLQueryGenerator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Class that manages the database create and upgrade, connections and defines some configuration
 * @author Mansour
 *
 */

public class DatabaseManager extends SQLiteOpenHelper {


    /**
     * Default constructor to create a manager for database connection
     *
     * @param context the application context
     */
	public DatabaseManager(Context context) {
	    super(context, Configuration.DATABASE_NAME, null, Configuration.DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase database) {
        SQLConsole.Log("Creating database file with name " + Configuration.DATABASE_NAME + " and version " + Configuration.DATABASE_VERSION);
        if (Configuration.DATABASE_CREATE.equals("create") || Configuration.DATABASE_CREATE.equals("drop-create")) {
            database.execSQL("PRAGMA FOREIGN_KEYS = OFF");
            try {
                Class[] classes = Reflections.getSubClassesOfClass(PersistentEntity.class, Configuration.DOMAIN_PACKAGE);
                for (Class<PersistentEntity> currentClass : classes) {
                    SQLConsole.Log(SQLQueryGenerator.getCreateTable(currentClass));
                    database.execSQL(SQLQueryGenerator.getCreateTable(currentClass));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            database.execSQL("PRAGMA FOREIGN_KEYS = ON");
            validateDatabase(database);
        }
	}
        
    @Override
    public void onUpgrade(SQLiteDatabase database,  int oldVersion, int newVersion) {
        SQLConsole.Log("Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        if (Configuration.DATABASE_CREATE.equals("drop-create")) {
            database.execSQL("PRAGMA FOREIGN_KEYS = OFF");
            try {
                Class[] classes = Reflections.getSubClassesOfClass(PersistentEntity.class, Configuration.DOMAIN_PACKAGE);
                for (Class<PersistentEntity> currentClass : classes) {
                    SQLConsole.Log(SQLQueryGenerator.getDropTable(currentClass));
                    database.execSQL(SQLQueryGenerator.getDropTable(currentClass));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            onCreate(database);
        } else if (Configuration.DATABASE_CREATE.equals("update")) {
            database.execSQL("PRAGMA FOREIGN_KEYS = OFF");
            ArrayList<Table> newTables = getDomainStructure();
            ArrayList<Table> oldTables = getDatabaseStructure(database);
            for (Table oldTable : oldTables) {
                if (!newTables.contains(oldTable)) {
                    String dropTableQuery = "DROP TABLE IF EXISTS " + oldTable.name;
                    SQLConsole.Log(dropTableQuery);
                    database.execSQL(dropTableQuery);
                } else {
                    // TODO: comprobar campos
                }
            }
            for (Table newTable : newTables) {
                if (!oldTables.contains(newTable)) {
                    String createTableQuery = SQLQueryGenerator.getCreateTable(newTable.type);
                    SQLConsole.Log(createTableQuery);
                    database.execSQL(createTableQuery);
                }
            }
            database.execSQL("PRAGMA FOREIGN_KEYS = ON");
            validateDatabase(database);
        }
    }

    public void clearDatabase () {
        SQLiteDatabase database = getWritableDatabase();
        onUpgrade(database, 0, Configuration.DATABASE_VERSION);
        database.close();
    }

    public void validateDatabase (SQLiteDatabase database) {
        Cursor integrityCheckCursor = database.rawQuery("PRAGMA INTEGRITY_CHECK", null);
        Cursor foreignKeysCheckCursor = database.rawQuery("PRAGMA FOREIGN_KEYS_CHECK", null);
        printCursor(integrityCheckCursor);
        printCursor(foreignKeysCheckCursor);
    }

    public ArrayList<Table> getDatabaseStructure(SQLiteDatabase database) {
        Cursor tableCursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name!='android_metadata' AND name!='sqlite_sequence'", null);
        ArrayList<Table> result = new ArrayList<Table>();
        for (tableCursor.moveToFirst(); !tableCursor.isAfterLast(); tableCursor.moveToNext()) {
            Table table = new Table();
            table.name = tableCursor.getString(0);

            ArrayList<Column> columns = new ArrayList<Column>();

            Cursor columnsCursor  = database.rawQuery("PRAGMA TABLE_INFO(" + table.name + ")", null);
            for (columnsCursor.moveToFirst(); !columnsCursor.isAfterLast(); columnsCursor.moveToNext()) {
                Column column = new Column();
                column.name = columnsCursor.getString(columnsCursor.getColumnIndex("name"));
                column.type = columnsCursor.getString(columnsCursor.getColumnIndex("type"));
                columns.add(column);
            }
            columnsCursor.moveToFirst();

            table.columns = columns;
            result.add(table);
        }
        return result;
    }

    public ArrayList<Table> getDomainStructure() {
        ArrayList<Table> result = new ArrayList<Table>();
        try {
            Class[] classes = Reflections.getSubClassesOfClass(PersistentEntity.class, Configuration.DOMAIN_PACKAGE);
            for (Class<PersistentEntity> currentClass : classes) {
                Table table = new Table();
                table.name = SQLQueryGenerator.getTableName(currentClass);
                table.type = currentClass;
                ArrayList<Column> columns = new ArrayList<Column>();

                List<Field> columnFields = EntityFieldHelper.getColumnFields(currentClass);
                for (Field columnField : columnFields) {
                    if (EntityFieldHelper.isPrimitiveField(columnField) || (EntityFieldHelper.isSingleRelationField(columnField) && !columnField.isAnnotationPresent(HasOne.class))) {
                        Column column = new Column();
                        column.name = SQLQueryGenerator.getColumnName(columnField);
                        column.type = SQLQueryGenerator.getColumnType(columnField);
                        columns.add(column);
                    }
                }

                table.columns = columns;
                result.add(table);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void printCursor(Cursor cursor) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String rowValue = "";
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                rowValue += " | " + cursor.getString(i);
            }
            SQLConsole.Log(rowValue);
        }
    }

    public class Table {
        public String name;
        public Class<PersistentEntity> type;
        public ArrayList<Column> columns;

        @Override
        public boolean equals(Object o) {
            return name.equals(((Table) o).name);
        }
    }
    public class Column {
        public String name;
        public String type;
    }
}
    