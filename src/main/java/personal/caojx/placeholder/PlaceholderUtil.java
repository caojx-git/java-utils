package personal.caojx.placeholder;


import org.apache.commons.text.StringSubstitutor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 占位符替换，工具类
 *
 * @author caojx
 * @version $Id: PlaceholderUtil.java,v 1.0 2019-05-09 20:32 caojx
 * @date 2019-05-09 20:32
 */
public class PlaceholderUtil {

    public static void main(String[] args) {
        //方式1
        String templateResult1 = String.format("%s is at the age of %s", "john", "26");
        System.out.println(templateResult1);

        //方式2
        Object[] object = new Object[]{"john", "24"};
        MessageFormat messageFormat = new MessageFormat("{0} is at the age of {1}");
        String templateResult2 = messageFormat.format(object);
        System.out.println(templateResult2);

        //方式3
        /*
         *  引入commons-text
         *   <dependency>
         *      <groupId>org.apache.commons</groupId>
         *       <artifactId>commons-text</artifactId>
         *      <version>1.6</version>
         *   </dependency>
         * */
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "john");
        paramMap.put("age", "27");
        //org.apache.commons.text.StringSubstitutor
        StringSubstitutor stringSubstitutor = new StringSubstitutor(paramMap);
        String template3 = "${name} is at the age of ${age}";
        String templateResult3 = stringSubstitutor.replace(template3);
        System.out.println(templateResult3);
    }
}