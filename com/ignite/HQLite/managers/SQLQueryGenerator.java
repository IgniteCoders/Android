package com.ignite.HQLite.managers;

import com.ignite.HQLite.PersistentEntity;
import com.ignite.HQLite.annotations.Constraints;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mansour on 13/05/2016.
 */

/**
 * Class that uses reflection to generate SQL code from persistent entities
 *
 * @author Mansour
 * @version 1.0
 */
public class SQLQueryGenerator {

    /******************************************************** Table queries */

    /**
     * Generate the <code>CREATE TABLE<code> query from a <code>PersistentEntity</code> class
     * @param domainClass the class of the persistent entity, must extends <code>PersistentEntity</code>
     * @return the generated <code>CREATE TABLE<code> query
     */
    public static String getCreateTable(Class<PersistentEntity> domainClass) {
        List<Field> fields = EntityFieldHelper.getColumnFields(domainClass);
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + getTableName(domainClass) + " (_id INTEGER PRIMARY KEY AUTOINCREMENT";
        for (int i = 0; i < fields.size(); i ++) {
            Field field = fields.get(i);
            if (!EntityFieldHelper.isCollectionRelationField(field)) {
                sqlCreate += ", " + getColumnDeclaration(field);
            }
        }
        sqlCreate += ");";
//        TODO: Cuidado con estos replace que son solo esteticos y pueden joder la sentencia
        return sqlCreate.replace("  ", " ").replace(" ,", ",");
    }

    /**
     * Generate the <code>DROP TABLE<code> query from a <code>PersistentEntity</code> class
     * @param domainClass the class of the persistent entity, must extends <code>PersistentEntity</code>
     * @return the generated <code>DROP TABLE<code> query
     */
    public static String getDropTable(Class<PersistentEntity> domainClass) {
        return "DROP TABLE IF EXISTS " + getTableName(domainClass);
    }


    /**
     * Generate the table name for SQL from a <code>PersistentEntity</code> class
     * @param domainClass the class of the persistent entity, must extends <code>PersistentEntity</code>
     * @return the table name
     */
    public static String getTableName(Class<PersistentEntity> domainClass) {
        return domainClass.getSimpleName();
    }

    /******************************************************** Column queries */

    /**
     * Generate the column declaration for SQL from a <code>Field</code>
     * @param field the field of the persistent entity
     * @return the column declaration as "field name" + "field type" + "field constraints"
     */
    public static String getColumnDeclaration(Field field) {
        return (getColumnName(field) + " " + getColumnType(field) + " " + getColumnConstraints(field));
    }

    /**
     * Generate the column name for SQL from a <code>Field</code>
     * @param field the field of the persistent entity
     * @return the column name. If the field is a single relationship, this method returns "idField" instead of "field"
     */
    public static String getColumnName(Field field) {
        if (EntityFieldHelper.isPrimitiveField(field)) {
            return field.getName();
        } else if (EntityFieldHelper.isSingleRelationField(field)) {
            return "id" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        } else {
            return field.getName();
        }
    }

    /**
     * Generate a collection with the columns names for SQL from a <code>PersistentEntity</code>
     * @param domainClass the class of the persistent entity, must extends <code>PersistentEntity</code>
     * @return List of columns names
     */
    public static List<String> getColumnNames(Class<PersistentEntity> domainClass) {
        List<Field> fields = EntityFieldHelper.getColumnFields(domainClass);
        List<String> columns = new ArrayList<String>();
        columns.add(DatabaseManager.ID_COLUMN_NAME);
        for (Field field : fields) {
            if (!EntityFieldHelper.isCollectionRelationField(field)) {
                columns.add(getColumnName(field));
            }
        }
        return columns;
    }

    /**
     * Generate the column type for SQL from a <code>Field</code> type
     * @param field the field of the persistent entity
     * @return the column type. If the field is a single relationship, this method returns "LONG" to store the id
     */
    public static String getColumnType(Field field) {
        String result = "";
        if (field.getType() == String.class) {
            result += "TEXT";
        } else if (field.getType() == long.class) {
            result += "LONG";
        } else if (field.getType() == double.class) {
            result += "DOUBLE";
        } else if (field.getType() == int.class) {
            result += "INTEGER";
        } else if (field.getType() == boolean.class) {
            result += "BOOLEAN";
        } else if (field.getType() == byte[].class) {
            result += "BLOB";
        } else if (EntityFieldHelper.isRelationField(field)) {
            if (EntityFieldHelper.isSingleRelationField(field)) {
                result += "LONG";
            }
        }
        return result;
    }

    /**
     * Generate the column constraints for SQL from a <code>Field</code> if <code>Constraint</code> annotation is present
     * @param field the field of the persistent entity
     * @return the column constraints
     */
    public static String getColumnConstraints(Field field) {
        String result = "";
        if (field.isAnnotationPresent(Constraints.class)) {
            Constraints constraints = field.getAnnotation(Constraints.class);
            if (constraints.unique() == true) {
                result += " UNIQUE";
            }
            if (constraints.nullable() == false) {
                result += " NOT NULL";
            }
        }
        return result;
    }
}
