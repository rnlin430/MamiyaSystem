package com.github.rnlin.system.mamiya;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;

public class Reflection {

    // Dynamically getting a value from a private memver.
    protected static <T extends Object, U> U getValue(@NotNull T obj, @NotNull String fieldname)
            throws IllegalAccessException, NoSuchFieldException{
        Field field = null;
        field = Reflection.<T>getField(obj, fieldname);
        field.setAccessible(true);
        return (U) field.get(obj);
    }

    // Dynamically setting a value from a private member.
    protected static <T extends Object> void setValue(@NotNull T obj, @NotNull String fieldname, @NotNull String value)
            throws IllegalAccessException ,NoSuchFieldException{
        Field field = null;
        field = Reflection.<T>getField(obj, fieldname);
        field.setAccessible(true);
        field.set(obj, value);
    }

    protected static <T extends Object> Field getField(T obj, String fieldname) throws NoSuchFieldException {
        return getClass(obj).getDeclaredField(fieldname);
    }

    protected static Class<?> getClass(Object obj) {
        return obj.getClass();
    }
}
