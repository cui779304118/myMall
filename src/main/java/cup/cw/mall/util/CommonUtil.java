package cup.cw.mall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
/**
 * created by cuiwei on 2018/11/9
 * 普通工具类
 */
public class CommonUtil {
    private static Logger logger = LoggerFactory.getLogger(CommonUtil.class);


    public static <E> Map<String,Object> beanToMap(E e) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Map<String,Object> retMap = new HashMap<>();
        Class type = e.getClass();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)){
                Method readMethod = descriptor.getReadMethod();
                Object value = readMethod.invoke(e,new Object[0]);
                if (value != null){
                    retMap.put(propertyName,value);
                }else {
                    retMap.put(propertyName,"");
                }
            }
        }
        return retMap;
    }

}
