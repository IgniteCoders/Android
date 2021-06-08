package com.ignite.HQLite.utils;

import com.ignite.HQLite.Configuration;
import com.ignite.HQLite.PersistentEntity;
import com.ignite.HQLite.annotations.Constraints;
import com.ignite.HQLite.annotations.HasOne;

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
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + getTableName(domainClass) + " (" + Configuration.ID_COLUMN_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT";
        for (int i = 0; i < fields.size(); i ++) {
            Field field = fields.get(i);
            if (!EntityFieldHelper.isCollectionRelationField(field) && !field.isAnnotationPresent(HasOne.class)) {
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
     * Build an SQL query string from the given clauses.
     *
     * param distinct true if you want each row to be unique, false otherwise.
     * @param table The table name to compile the query against.
     * @param columns A list of which columns to return. Passing null will
     *            return all columns, which is discouraged to prevent reading
     *            data from storage that isn't going to be used.
     * param where A filter declaring which rows to return, formatted as an SQL
     *            WHERE clause (excluding the WHERE itself). Passing null will
     *            return all rows for the given URL.
     * param groupBy A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * param having A filter declare which row groups to include in the cursor,
     *            if row grouping is being used, formatted as an SQL HAVING
     *            clause (excluding the HAVING itself). Passing null will cause
     *            all row groups to be included, and is required when row
     *            grouping is not being used.
     * param orderBy How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * param limit Limits the number of rows returned by the query,
     *            formatted as LIMIT clause. Passing null denotes no LIMIT clause.
     * @return the SQL query string
     */
    public static String getSelectQuery(String table, String[] columns, CriteriaBuilder criteria) {
        if ((criteria.groupBy() == null || criteria.groupBy().isEmpty()) && !(criteria.having() == null || criteria.having().isEmpty())) {
            throw new IllegalArgumentException("HAVING clauses are only permitted when using a GROUP BY clause");
        }
        /*if (!String.IsNullOrEmpty (limit) && !limitPattern.Match (limit).Success) {
            throw new IllegalArgumentException ("invalid LIMIT clauses:" + limit);
        }*/

        StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append("SELECT ");
        if (criteria.distinct()) {
            sqlQuery.append("DISTINCT ");
        }
        //sqlQuery.append(Configuration.ID_COLUMN_NAME);

        if (columns != null && columns.length != 0) {
            for (int i = 0; i < columns.length; i++) {
                sqlQuery.append((i == 0 ? "" : ", ") + (columns[i].equals("COUNT(*)") ? "" : table + ".") + columns[i]);
            }
        }

        sqlQuery.append (" FROM " + table);
        appendClause (sqlQuery, "", criteria.join());
        appendClause (sqlQuery, "WHERE", criteria.query());
        appendClause (sqlQuery, "GROUP BY", criteria.groupBy());
        appendClause (sqlQuery, "HAVING", criteria.having());
        appendClause (sqlQuery, "ORDER BY", criteria.orderBy());
        appendClause (sqlQuery, "LIMIT", criteria.limit());

        return sqlQuery.toString();
    }

    private static void appendClause (StringBuilder sqlQuery, String name, String clause) {
        if (!(clause == null || clause.isEmpty())) {
            sqlQuery.append (" " + name + " " + clause);
        }
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
        columns.add(Configuration.ID_COLUMN_NAME);
        for (Field field : fields) {
            if (!EntityFieldHelper.isCollectionRelationField(field) && !field.isAnnotationPresent(HasOne.class)) {
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
        } else if (field.getType() == long.class || field.getType() == Long.class) {
            result += "LONG";
        } else if (field.getType() == double.class || field.getType() == Double.class) {
            result += "DOUBLE";
        } else if (field.getType() == int.class || field.getType() == Integer.class) {
            result += "INTEGER";
        } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            result += "BOOLEAN";
        } else if (field.getType() == byte[].class || field.getType() == Byte[].class) {
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
    private static String getColumnConstraints(Field field) {
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
//        if (EntityFieldHelper.isSingleRelationField(field) && !field.isAnnotationPresent(HasOne.class)) {
//            result += " REFERENCES " + getTableName((Class<PersistentEntity>) field.getType()) + "(" + Configuration.ID_COLUMN_NAME + ")";
//            if (field.isAnnotationPresent(BelongsTo.class)) {
//                result += " ON DELETE CASCADE";
//            }
//        }
        return result;
    }
}
