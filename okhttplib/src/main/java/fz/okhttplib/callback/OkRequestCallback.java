package fz.okhttplib.callback;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Create by JFZ
 * date: 2019-04-22 10:30
 **/
public abstract class OkRequestCallback<T> {
    public Type mType;

    public OkRequestCallback() {
        mType = getSuperclassTypeParameter(getClass());
    }

    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("jfz_exception:Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized
                .getActualTypeArguments()[0]);
    }

    public abstract void onResponse(T data);

    public abstract void onError(int code, Exception e);
}
