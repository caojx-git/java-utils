package personal.caojx.reflect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * ReflectConvertHelper 属性复制工具类
 *
 * @author halley.qiu
 * @version $Id: ReflectConvertHelper.java,v 1.0 2018-06-27 18:41 halley.qiu
 * @date 2018/06/27 18:41
 */
@Slf4j
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
     * 属性拷贝
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
}
