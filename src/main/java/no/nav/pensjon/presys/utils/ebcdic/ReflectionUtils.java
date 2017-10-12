package no.nav.pensjon.presys.utils.ebcdic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

class ReflectionUtils {

    private ReflectionUtils(){}

    private static Method getGetter(Field f) {
        String getterName = prefixFieldName(f, "get");
        try {
            return f.getDeclaringClass().getMethod(getterName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format("Fant ikke getter for felt %s på segment %s",f.getName(),f.getDeclaringClass().getSimpleName() ), e);
        }
    }

    static Object get(Field f, Object o){
        try {
            return getGetter(f).invoke(o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(String.format("Kunne ikke kalle getter for %s på segment %s",  f.getName(), f.getDeclaringClass().getSimpleName()), e);
        }
    }

    static void set(Object v, Field f, Object o){
        try {
            getSetter(f).invoke(o, v);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Kunne ikke kalle setter for %s på segment %s", f.getName(), f.getDeclaringClass().getSimpleName()), e);
        }
    }

    private static String prefixFieldName(Field f, String prefix){
        return prefix + String.valueOf(f.getName().charAt(0)).toUpperCase() + f.getName().substring(1);
    }

    static Class<?> findSubType(Field f){
        if(f.getType().equals(List.class)){
            ParameterizedType pt = (ParameterizedType) f.getGenericType();
            return  (Class<?>) pt.getActualTypeArguments()[0];
        }else{
            return f.getType();
        }

    }

    private static Method getSetter(Field f) {
        String setterName = prefixFieldName(f, "set");
        try {
            return f.getDeclaringClass().getMethod(setterName, f.getType());
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Fant ikke setter for felt " + f.getName() +  "på segment "  + f.getDeclaringClass().getSimpleName(), e);
        }
    }

    static <E> void setObjectOnField(E o, Field f, Object valueToSet) {
        try {
            getSetter(f).invoke(o, valueToSet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
