package com.ignite.HQLite.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Base64;

import com.ignite.HQLite.PersistentEntity;
import com.ignite.HQLite.annotations.BelongsTo;
import com.ignite.HQLite.annotations.HasMany;
import com.ignite.HQLite.annotations.HasOne;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Mansour on 13/05/2016.
 */
public class EntityFieldHelper {

    /******************************************************** Field setter/getter */
    /* setter */
    public static PersistentEntity setFieldFromValue(PersistentEntity object, Field field, Object value) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(value, object);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }
    public static PersistentEntity setFieldFromJSONObject(PersistentEntity object, Field field, JSONObject _JSONObject) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            Object value = null;
            String columnName = SQLQueryGenerator.getColumnName(field);
            if (EntityFieldHelper.isRelationField(field)) {
                Class relationClass = field.getType();
                if (EntityFieldHelper.isSingleRelationField(field)) {
                    PersistentEntity relationObject = (PersistentEntity)relationClass.newInstance();
                    try {
                        JSONObject _JSONObjectChild = _JSONObject.getJSONObject(field.getName());
                        value = relationObject.getTableData().parse(_JSONObjectChild);
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    }
                    try {
                        Long relationId = _JSONObject.getLong(columnName);
                        relationObject.setServerId(relationId);
                        value = relationObject;
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    }
                } else {
                    try {
                        JSONArray _JSONArray = _JSONObject.getJSONArray(columnName);
                        Type type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                        PersistentEntity relationObject = (PersistentEntity) Reflections.getClassFromType(type).newInstance();
                        value = relationObject.getTableData().parse(_JSONArray);
                    } catch (JSONException e) {
                        //e.printStackTrace();
                        value = null;
                    }
                }
            } else {
                if (field.getType() == String.class) {
                    value = _JSONObject.getString(columnName);
                } else if (field.getType() == long.class) {
                    value = _JSONObject.getLong(columnName);
                } else if (field.getType() == double.class) {
                    value = _JSONObject.getDouble(columnName);
                } else if (field.getType() == int.class) {
                    value = _JSONObject.getInt(columnName);
                } else if (field.getType() == boolean.class) {
                    value = _JSONObject.getInt(columnName) != 0;
                } else if (field.getType() == byte[].class) {
                    try {
                        String base64Value = _JSONObject.getString(columnName);
                        if (base64Value != null && !base64Value.equals("")) {
                            value = Base64.decode(base64Value, Base64.DEFAULT);
                        }
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        //e.printStackTrace();
                    }
                }
            }
            field.set(object, value);

            field.setAccessible(accessible);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static PersistentEntity setFieldFromCursor(PersistentEntity object, Field field, Cursor cursor) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            Object value = null;
            String columnName = SQLQueryGenerator.getColumnName(field);
            if (EntityFieldHelper.isRelationField(field)) {
                Class relationClass = field.getType();
                if (EntityFieldHelper.isSingleRelationField(field)) {
                    int columnIndex = cursor.getColumnIndex(columnName);
                    Long relationId = cursor.getLong(columnIndex);
                    PersistentEntity relationObject = (PersistentEntity)relationClass.newInstance();
//                  TODO: mappedBy se puede usar aqui
                    if (field.isAnnotationPresent(BelongsTo.class)) {
                        relationObject.setServerId(relationId);
                        value = relationObject;
                    } else {
                        value = relationObject.getTableData().getByServerId(relationId);
                    }
                } else {
                    Type type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                    PersistentEntity relationObject = (PersistentEntity) Reflections.getClassFromType(type).newInstance();
                    if (field.isAnnotationPresent(HasMany.class)) {
                        HasMany hasMany = field.getAnnotation(HasMany.class);
                        String mappedBy = hasMany.mappedBy();
                        value = relationObject.getTableData().getAllByField("id" + mappedBy.substring(0, 1).toUpperCase() + mappedBy.substring(1), object.getServerId());
                    } else {
                        value = new ArrayList<>(); //TODO: Esta lista es de uso y no se maneja aun (hay que crear una tabla auxiliar)
                    }
                }
            } else {
                int columnIndex = cursor.getColumnIndex(columnName);
                if (field.getType() == String.class) {
                    value = cursor.getString(columnIndex);
                } else if (field.getType() == long.class) {
                    value = cursor.getLong(columnIndex);
                } else if (field.getType() == double.class) {
                    value = cursor.getDouble(columnIndex);
                } else if (field.getType() == int.class) {
                    value = cursor.getInt(columnIndex);
                } else if (field.getType() == boolean.class) {
                    value = cursor.getInt(columnIndex) != 0;
                } else if (field.getType() == byte[].class) {
                    value = cursor.getBlob(columnIndex);
                }
            }
            field.set(object, value);

            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return object;
    }

    /* getter */
    public static JSONObject setFieldInJSONObject(PersistentEntity object, Field field, JSONObject _JSONObject) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            Object value = field.get(object);
            if (EntityFieldHelper.isRelationField(field)) {
                if (EntityFieldHelper.isSingleRelationField(field)) {
                    PersistentEntity relationObject = (PersistentEntity) value;
                    if (field.isAnnotationPresent(BelongsTo.class)) {
                        _JSONObject.put(SQLQueryGenerator.getColumnName(field), relationObject.getServerId());
                    } else {
                        _JSONObject.put(field.getName(), relationObject.getTableData().wrap(relationObject));
                    }
                } else {
                    Type type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                    PersistentEntity relationObject = (PersistentEntity) Reflections.getClassFromType(type).newInstance();
                    List<PersistentEntity> relationList = (List) value;
                    _JSONObject.put(field.getName(), relationObject.getTableData().wrap(relationList));
                }
            } else {
                if (field.getType() == String.class) {
                    _JSONObject.put(field.getName(), (String) value);
                } else if (field.getType() == long.class) {
                    _JSONObject.put(field.getName(), (Long) value);
                } else if (field.getType() == double.class) {
                    _JSONObject.put(field.getName(), (Double) value);
                } else if (field.getType() == int.class) {
                    _JSONObject.put(field.getName(), (Integer) value);
                } else if (field.getType() == boolean.class) {
                    _JSONObject.put(field.getName(), (Boolean) value);
                } else if (field.getType() == byte[].class) {
                    _JSONObject.put(field.getName(), Base64.encodeToString((byte[]) value, Base64.NO_WRAP));
                }
            }

            field.setAccessible(accessible);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return _JSONObject;
    }

    public static ContentValues setFieldInContentValues(PersistentEntity object, Field field, ContentValues values) {
        try {
            boolean accessible = field.isAccessible();
            field.setAccessible(true);

            Object value = field.get(object);
            if (EntityFieldHelper.isRelationField(field)) {
                if (EntityFieldHelper.isSingleRelationField(field)) {
                    PersistentEntity relationObject = (PersistentEntity) value;
                    if (field.isAnnotationPresent(BelongsTo.class) || !field.isAnnotationPresent(HasOne.class)) {
                        values.put(SQLQueryGenerator.getColumnName(field), relationObject.getServerId());
                    }
                } else {

                }
            } else {
                if (field.getType() == String.class) {
                    values.put(field.getName(), (String) value);
                } else if (field.getType() == long.class) {
                    values.put(field.getName(), (Long) value);
                } else if (field.getType() == double.class) {
                    values.put(field.getName(), (Double) value);
                } else if (field.getType() == int.class) {
                    values.put(field.getName(), (Integer) value);
                } else if (field.getType() == boolean.class) {
                    values.put(field.getName(), (Boolean) value);
                } else if (field.getType() == byte[].class) {
                    values.put(field.getName(), (byte[]) value);
                }
            }

            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return values;
    }

    /******************************************************** Column utils */

    /* Return fields that are handled as columns in table*/
    public static List<Field> getColumnFields(Class<PersistentEntity> domainClass) {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : domainClass.getDeclaredFields()) {
            if (isColumn(field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    /* Return fields that are handled as columns in table*/
    public static List<Field> getRelationFields(Class<PersistentEntity> domainClass) {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : domainClass.getDeclaredFields()) {
            if (isRelationField(field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    /* Return true if field is handled as column in table*/
    public static boolean isColumn(Field field){
        return isPrimitiveField(field) || isRelationField(field);
    }

    /* Return true if field is handled as primitive column in table*/
    public static boolean isPrimitiveField(Field field) {
        Class type = field.getType();
        if (type == String.class || type == long.class || type == double.class || type == int.class || type == boolean.class || type == byte[].class) {
            return true;
        } else {
            return false;
        }
    }

    /* Return true if field is handled as foreign key column in table*/
    public static boolean isRelationField(Field field) {
        return isSingleRelationField(field) || isCollectionRelationField(field);
    }

    /* Return true if field is a foreign key (hasOne, belongsTo)*/
    public static boolean isSingleRelationField(Field field) {
        if (PersistentEntity.class.isAssignableFrom(field.getType())) {
            return true;
        } else {
            return false;
        }
    }

    /* Return true if field is a foreign key (hasMany)*/
    public static boolean isCollectionRelationField(Field field) {
        Class fieldType = field.getType();
        if (Collection.class.isAssignableFrom(fieldType)) {
            try {
                Type type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                if (PersistentEntity.class.isAssignableFrom(Reflections.getClassFromType(type))) {
                    return true;
                } else {
                    return false;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}