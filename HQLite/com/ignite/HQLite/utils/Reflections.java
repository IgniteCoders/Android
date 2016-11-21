package com.ignite.HQLite.utils;

import android.content.Context;

import com.ignite.HQLite.PersistentEntity;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by Mansour on 11/05/2016.
 */
public class Reflections {

    public static Class getClassFromType(Type type) throws ClassNotFoundException {
        return Class.forName(type.toString().split(" ")[1]);
    }

    public static Type[] getTypeArgumentsFromClass(Class typedClass) throws ClassNotFoundException {
        Type superClassType = typedClass.getGenericSuperclass();
        return ((ParameterizedType) superClassType).getActualTypeArguments();
    }

    public static Class getClassFromTypeOfClass(Class typedClass) throws ClassNotFoundException {
        Type type = getTypeArgumentsFromClass(typedClass)[0];
        return getClassFromType(type);
    }

    public static List<Field> getDeclaredFieldsRecursively(Class extensionClass, Class extendedClass) {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : extensionClass.getDeclaredFields()) {
            fields.add(field);
        }
        if (extensionClass.getSuperclass() != extendedClass) {
            fields.addAll(getDeclaredFieldsRecursively(extensionClass.getSuperclass(), extendedClass));
        }
        return fields;
    }

    public static Field getDeclaredFieldRecursively(String name, Class extensionClass, Class extendedClass) {
        List<Field> fields = getDeclaredFieldsRecursively(extensionClass, extendedClass);
        Field field = null;
        for (Field recursiveField : fields) {
            if (recursiveField.getName().equals(name)) {
                field = recursiveField;
                break;
            }
        }
        return field;
    }

    public static Object fillFieldFromObjectToObject(Field field, Object fromObject, Object toObject) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(toObject, field.get(fromObject));
        field.setAccessible(accessible);
        return toObject;
    }

    public static Object getSuperInstanceFromInstance(Object object) throws IllegalAccessException, InstantiationException {
        Class superClass = object.getClass().getSuperclass();
        /*if (superClass != PersistentEntity.class) {
        if (superClass.getSuperclass() != PersistentEntity.class) {

        }*/
        Object superObject = superClass.newInstance();
        for (Field field : getDeclaredFieldsRecursively(superClass, PersistentEntity.class)/*superClass.getDeclaredFields()*/){
            superObject = fillFieldFromObjectToObject(field, object, superObject);
        }
        return superObject;
    }

    public static Object setInstanceFromSuperInstance(Object object, Object superObject) throws IllegalAccessException {
        for (Field field : superObject.getClass().getDeclaredFields()){
            object = fillFieldFromObjectToObject(field, superObject, object);
        }
        return object;
    }

    public static Class[] getSubClassesOfClass(Class parentClass) throws IOException {
        return getSubClassesOfClass(parentClass, "");
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @param parentClass The parent class
     * @return The classes that extends from parentClass
     * @throws IOException
     */
    public static Class[] getSubClassesOfClass(Class parentClass, String packageName) throws IOException {
        Context context = ApplicationContextProvider.getContext();
        ArrayList<Class> classes = new ArrayList<Class>();
        String packageCodePath = context.getPackageCodePath();
        System.out.println(packageCodePath);
        DexFile df = new DexFile(packageCodePath);
        for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
            String className = iter.nextElement();
            if (className.contains(packageName)) {
                try {
                    Class candidateClass = Class.forName(className);
                    if (parentClass.isAssignableFrom(candidateClass) && parentClass != candidateClass) {
                        SQLConsole.Log(className);
                        classes.add(candidateClass);
                    }
                } catch (NoClassDefFoundError e) {
                    //e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    //e.printStackTrace();
                } catch (Exception e) {
                    //e.printStackTrace();
                } catch (Throwable e) {
                    //e.printStackTrace();
                }
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }
}