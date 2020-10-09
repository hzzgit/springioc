package com.yy.vo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ：hzz
 * @description：TODO
 * @date ：2020/10/9 17:19
 */
public class PostClas {
    private Method method;
    private Object  object;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void runBegin(){
        try {
            method.invoke(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
