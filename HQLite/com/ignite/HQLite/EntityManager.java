package com.ignite.HQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.ignite.HQLite.utils.ApplicationContextProvider;
import com.ignite.HQLite.utils.EntityFieldHelper;
import com.ignite.HQLite.utils.Reflections;
import com.ignite.HQLite.utils.SQLConsole;
import com.ignite.HQLite.utils.SQLQueryGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface used to interact with the persistence context, SQLite in this case.
 *
 * The EntityManager API is used to create and remove persistent entity instances, to find entities by their primary key, and to query over entities.
 */
public abstract class EntityManager<E extends PersistentEntity> {
	private String tableName = null;
	private String[] columnNames = null;
	private Class domainClass = null;
	private List<Field> columnFields = null;

    private Context context;
    private SQLiteDatabase database;
    private DatabaseManager databaseManager;

    /**
     * Open a new database connection
     */
    private void open() throws SQLException {
        context = ApplicationContextProvider.getContext();
        databaseManager = new DatabaseManager(context);
        database = databaseManager.getReadableDatabase();
    }

    /**
     * Close the database connection
     */
    private void close() {
        databaseManager.close();
    }

    /** Retrieve the domain class <code>E extends PersistentEntity</code> from the types arguments.
     *
     * @return the domain class associated to this entity manager.
     */
    public Class getDomainClass() {
        if (domainClass == null) {
            try {
                domainClass = Reflections.getClassFromTypeOfClass(this.getClass());;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return domainClass;
    }

    /** Retrieve the domain class fields that will be used in persistence context.
     *
     * @return a list of fields from the domain class that will be used in persistence context.
     */
    public List<Field> getColumnFields() {
		if (columnFields == null) {
            columnFields = EntityFieldHelper.getColumnFields(this.getDomainClass());
		}
		return columnFields;
	}

    /** Generate the name of the domain class.
     *
     * @return the name of the domain class that will be used in persistence context.
     */
	public String getTableName() {
		if (tableName == null) {
            tableName = SQLQueryGenerator.getTableName(this.getDomainClass());
		}
		return tableName;
	}

    /** Generate a list with the columns names from the fields.
     *
     * @return a list with the columns names.
     */
	public String[] getColumnNames() {
		if (columnNames == null) {
			List<String> columns = SQLQueryGenerator.getColumnNames(this.getDomainClass());
            columnNames = columns.toArray(new String[columns.size()]);
		}
		return columnNames;
	}

    /** Creates an object from the stored data in the cursor.
     *
     * @param cursor with the data of the query. It's should be pointing to the needed row.
     * @return the generated instance.
     */
	public E cursorToEntity (Cursor cursor) {
		try {
			Class domainClass = this.getDomainClass();
			E object = (E) domainClass.newInstance();

			object.setId(cursor.getLong(cursor.getColumnIndex(Configuration.ID_COLUMN_NAME)));
			for (Field field : this.getColumnFields()) {
                object = (E) EntityFieldHelper.setFieldFromCursor(object, field, cursor);
			}

            PersistentEntity superObject = getSuperObject(object.getId());
            if (superObject != null) {
                Reflections.setInstanceFromSuperInstance(object, superObject);
            }

			return object;
		} catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }
	}

    /** Creates a <code>ContentValues</code> from the object.
     *
     * @param object to be converted in a <code>ContentValues</code> for persistent context
     * @return the generated <code>ContentValues</code>.
     */
	public ContentValues getContentValues (E object) {
        ContentValues values = new ContentValues();
        if (object.getId() != null) {
            values.put(Configuration.ID_COLUMN_NAME, object.getId());
        }
        for (Field field : this.getColumnFields()) {
            if (!EntityFieldHelper.isCollectionRelationField(field)) {
                values = EntityFieldHelper.setFieldInContentValues(object, field, values);
            }
        }
        return values;
	}

    /** Creates an object from the stored data in the JSON.
     *
     * @param _JSONObject with the data of the object.
     * @return the generated instance.
     */
    public E parse(JSONObject _JSONObject) {
        try {
            Class domainClass = this.getDomainClass();
            E object = (E) domainClass.newInstance();

            for (Field field : this.getColumnFields()) {
                object = (E) EntityFieldHelper.setFieldFromJSONObject(object, field, _JSONObject);
            }

            Class superClass = domainClass.getSuperclass();
            PersistentEntity superObject = null;
            if (superClass != PersistentEntity.class) {
                try {
                    superObject = ((PersistentEntity) superClass.newInstance()).getTableData().parse(_JSONObject);
                    Reflections.setInstanceFromSuperInstance(object, superObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /** Creates a list with objects from the stored data in the JSON.
     *
     * @param _JSONList with the data of the JSON array list.
     * @return the generated list.
     */
    public List<E> parse(JSONArray _JSONList) {
        List<E> list = null;
        if(_JSONList != null && _JSONList.length() > 0){
            list = new ArrayList<E>();
            for(int i = 0; i < _JSONList.length(); i++){
                try{
                    JSONObject JSONObject = _JSONList.getJSONObject(i);
                    E object = parse(JSONObject);
                    if(object != null){
                        list.add(object);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /** Creates a <code>JSONObject</code> from the object.
     *
     * @param object to be converted in a <code>JSONObject</code>.
     * @return the generated <code>JSONObject</code>.
     */
    public JSONObject wrap(E object) {
        JSONObject _JSONObject = new JSONObject();
        try {
            for (Field field : getColumnFields()) {
                _JSONObject = EntityFieldHelper.setFieldInJSONObject(object, field, _JSONObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _JSONObject;
    }

    /** Creates a <code>JSONArray</code> from the object.
     *
     * @param list to be converted in a <code>JSONArray</code>.
     * @return the generated <code>JSONArray</code>.
     */
    public JSONArray wrap(List<E> list) {
        JSONArray _JSONArray = new JSONArray();
        if (list != null) {
            for (E object : list) {
                _JSONArray.put(wrap(object));
            }
        }
        return _JSONArray;
    }

    private PersistentEntity getSuperObject(long id) {
        Class superClass = this.getDomainClass().getSuperclass();
        PersistentEntity superObject = null;
        if (superClass != PersistentEntity.class) {
            try {
                superObject = ((PersistentEntity) superClass.newInstance()).getTableData().get(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return superObject;
    }

    /**
     * Convenience method for persisting a object into the database.
     *
     * @param object the object to persist in the database
     * @throws SQLException
     * @return the row ID of the newly persisted object, or -1 if an error occurred
     */
    public long insert(E object) {
        open();
        SQLConsole.LogInsert(object);
        long insertedId = database.insertOrThrow(getTableName(), null, getContentValues(object));
        close();
        return insertedId;
    }

    /**
     * Convenience method for updating a object already inserted into the database.
     *
     * @param object the object to update in the database
     * @throws SQLException
     * @return the number of rows affected
     */
    public int update(E object) {
        open();
        SQLConsole.LogUpdate(object);
        int updatedRows = database.update(getTableName(), getContentValues(object), Configuration.ID_COLUMN_NAME + "=" + object.getId(), null);
        close();
        return updatedRows;
    }

    /**
     * Convenience method for removing objects from the database.
     *
     * @param object the object to remove from the database
     * @return the number of rows affected, 1 if deleted correctly, 0 otherwise
     */
    public int delete(E object) {
        open();
        SQLConsole.LogDelete(object);
        int deletedRows = database.delete(getTableName(), Configuration.ID_COLUMN_NAME + "=" + object.getId(), null);
        close();
        return deletedRows;
    }

    // Methods for retrieve rows from database
	public E get(long id) {
		return getByField(Configuration.ID_COLUMN_NAME, id);
	}

    public E getByServerId(long id) {
        return get(id);
    }

    public E getByField(String key, Object value) {
        open();
        E object = null;
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, getTableName(), getColumnNames(), key + "=" + value.toString(), null, null, null, null);
        SQLConsole.Log(sqlQry);
        Cursor cursor = database.query(getTableName(), getColumnNames(), key + "='" + value.toString() + "'", null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                object = cursorToEntity(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            close();
        }
        return object;
    }

	public List<E> getAll() {
        open();
		List<E> listObjects = new ArrayList<E>();
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, getTableName(), getColumnNames(), null, null, null, null, null);
        SQLConsole.Log(sqlQry);
        Cursor cursor = database.query(getTableName(), getColumnNames(), null, null, null, null, null);
		try{
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				E object = cursorToEntity(cursor);
				listObjects.add(object);
			}
		} catch (Exception e) {
            e.printStackTrace();
        } finally {
			cursor.close();
            close();
		}
		return listObjects;
	}

	public List<E> getAllByField(String key, Object value) {
		List<E> listObjects = new ArrayList<E>();
        open();
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, getTableName(), getColumnNames(), key + "=" + value.toString(), null, null, null, null);
        SQLConsole.Log(sqlQry);
        Cursor cursor = database.query(getTableName(), getColumnNames(), key + "=" + value.toString(), null, null, null, null);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                E object = cursorToEntity(cursor);
                listObjects.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            close();
        }
		return listObjects;
	}

	public List<E> getAllByFieldInList(String key, Object[] values) {
		List<E> listObjects = new ArrayList<E>();
        String inClause = "(";
        for(int i = 0; i < values.length; i++){
            inClause += values[i].toString();
            if(i != (values.length - 1)){
                inClause += ",";
            }
        }
        inClause += ")";

        open();
        String sqlQry = SQLiteQueryBuilder.buildQueryString(false, getTableName(), getColumnNames(), key + " in " + inClause, null, null, null, null);
        SQLConsole.Log(sqlQry);
        Cursor cursor = database.query(getTableName(), getColumnNames(), key + " in " + inClause, null, null, null, null);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                E object = cursorToEntity(cursor);
                listObjects.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            close();
        }
		return listObjects;
	}

    public List<E> getWithCriteria() {
        List<E> listObjects = new ArrayList<E>();
        open();
        Cursor cursor = database.query(getTableName(), getColumnNames(), null, null, null, null, null);
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                E object = cursorToEntity(cursor);
                listObjects.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
            close();
        }
        return listObjects;
    }
}