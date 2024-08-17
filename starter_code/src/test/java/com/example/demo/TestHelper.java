package com.example.demo;

import java.lang.reflect.Field;

public class TestHelper {

    public static void injectObjects(Object target, String fieldName, Object toInject) {
        boolean wasPrivate = false;

        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            if (!f.isAccessible()) {
                f.setAccessible(true);
                wasPrivate = true;
            }
            f.set(target, toInject);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (wasPrivate) {
                try {
                    Field f = target.getClass().getDeclaredField(fieldName);
                    f.setAccessible(false);
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
    }
}