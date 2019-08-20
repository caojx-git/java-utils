package personal.caojx.reflect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ReflectConvertHelper 属性复制工具类
 *
 * @author halley.qiu
 * @version $Id: ReflectConvertHelper.java,v 1.0 2018-06-27 18:41 halley.qiu
 * @date 2018/06/27 18:41
 */
public class ReflectConvertHelper {

    /**
     * 属性拷贝，跳过有值的目标对象的属性
     *
     * @param source 源对象
     * @param target 目标对象
     * @return
     */
    public static <T, K> T fieldCopyIgnoreHasValueTargeProperties(K source, T target) {
        return fieldCopy(source, target, true);
    }

    /**
     * 属性拷贝，覆盖有值的目标对象的属性
     *
     * @param source 源对象
     * @param target 目标对象
     * @return
     */
    public static <T, K> T fieldCopy(K source, T target) {
        return fieldCopy(source, target, false);
    }

    /**
     * 方式1：通过反射实现深拷贝
     *
     * @param source 源对象
     * @param target 目标对象
     * @return
     */
    private static <T, K> T fieldCopy(K source, T target, boolean ignoreHasValueTargetProperties) {
        System.out.println("begin to fieldCopy");
        Field[] sourceDeclaredFields = source.getClass().getDeclaredFields();
        Field[] sourceSuperDeclaredFields = source.getClass().getSuperclass().getDeclaredFields();
        Field[] sourceFields = ArrayUtils.addAll(sourceDeclaredFields, sourceSuperDeclaredFields);
        for (Field sourceField : sourceFields) {
            Field targetField = null;
            try {
                try {
                    targetField = target.getClass().getDeclaredField(sourceField.getName());
                } catch (Exception e) {
                    System.out.println("current class has not this Field, search it in base Class");
                    targetField = target.getClass().getSuperclass().getDeclaredField(sourceField.getName());
                }
                targetField.setAccessible(true);
                sourceField.setAccessible(true);
                Object obj = sourceField.get(source);
                if (obj == null) {
                    continue;
                }
                //跳过目标对象有值的属性
                if (ignoreHasValueTargetProperties) {
                    if (obj instanceof Collection) {
                        if (!CollectionUtils.isEmpty((Collection<?>) targetField.get(target))) {
                            continue;
                        }
                    } else if (targetField.get(target) != null) {
                        continue;
                    }
                }
                //设置目标对象属性的值
                targetField.set(target, obj);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("end to fieldCopy");
        return target;
    }

    /**
     * 方式2：通过序列化方法实现深拷贝
     * 注意每个需要序列化的类都要实现 Serializable 接口，如果有某个属性不需要序列化，可以将其声明为 transient，即将其排除在克隆属性之外
     *
     * @param source
     * @return
     */
    public static Object fieldCopyBySerialization(Object source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(source);
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 方式3：通过序列化方法实现深拷贝，与方式2一样需要实现序列化接口
     *
     * @param source
     * @return
     */
    public static Object fieldCopyBySerialization2(Object source) {
        return SerializationUtils.clone((Serializable) source);
    }


    /**
     * 方式4：通过对象转json再转对象实现深拷贝
     *
     * @param obj
     * @return
     */
    public static Object fieldCopyByJson(Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, obj.getClass());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        List list = new ArrayList<>();
        list.add("a");

        List list2 = new ArrayList();
        fieldCopy(list, list2);
        System.out.println(list == list2); //false

        List list3 = (List) fieldCopyByJson(list);
        System.out.println(list == list3); //false
    }
}
