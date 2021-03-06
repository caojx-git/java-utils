package personal.caojx.amount;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 金额工具类
 * <p>
 * 中文金额转数字参考文章：
 * https://www.oschina.net/question/930697_2287851?sort=default
 * https://www.917118.com/tool/rmb.html
 * https://blog.csdn.net/Moneywa/article/details/97233159
 * <p>
 * <p>
 * 数字金额转大写金额参考如下文章：
 * http://www.360doc.com/content/11/1014/16/7918039_156170950.shtml
 * https://blog.csdn.net/feichitianxia/article/details/92801678
 * https://blog.csdn.net/Michean/article/details/90217498
 *
 * @author caojx created on 2020/4/15 2:31 下午
 */
//@Slf4j
public class AmountUtils {

    /**
     * 纯数字、数字加带单位的金额正则
     */
    private static final Pattern numberAmountRegx = Pattern.compile("((^[0-9]+(.[0-9]+)?$)|((([1-9]\\d*[\\d,，]*\\.?\\d*)|(0\\.[0-9]+))(元|万元|万)))");

    /**
     * 中文大写金额正则 [壹贰叁肆伍陆柒捌玖拾]+[壹贰叁肆伍陆柒捌玖拾佰仟万亿元圆角分厘零整正]+
     */
    private static final Pattern chineseAmountRegx = Pattern.compile("[壹贰叁肆伍陆柒捌玖拾]+[壹贰叁肆伍陆柒捌玖拾佰仟万亿元圆角分厘零整正]+");

    /**
     * 阿拉伯数字对应大写表
     */
    private static final char[] numArray = new char[]{'零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'};

    /**
     * 金额单位数组
     */
    private static final char[] unitArray = new char[]{'整', '厘', '分', '角', '圆', '拾', '佰', '仟', '万', '亿'};


    /**
     * 从文本中提取金额，并将提取到的金额值转为元
     * <p>
     * 优先匹配数字类型的金额，匹配成功返回对应的数字金额，没有匹配到数字金额，则匹配中文大写金额
     *
     * @param content 文本
     * @return
     */
    public static BigDecimal getAmount(String content) {
//        log.info("Request to AmountUtils getAmount content :{}", content);

        if (StringUtils.isEmpty(content)) {
//            log.info("Response of AmountUtils getAmount final content :{} => result :{}", content, BigDecimal.ZERO);
            return BigDecimal.ZERO;
        }

        String numberAmountContent = getNumberAmountContent(content);
        if (StringUtils.isNotEmpty(numberAmountContent)) {
            BigDecimal amount = numberAmount2Number(numberAmountContent);
//            log.info("Response of AmountUtils getAmount final content :{} => result :{}", content, amount);
            return amount;
        }

        String chineseAmountContent = getChineseAmountContent(content);
        if (StringUtils.isNotEmpty(chineseAmountContent)) {
            BigDecimal amount = chineseAmount2Number(chineseAmountContent);
//            log.info("Response of AmountUtils getAmount final content :{} => result :{}", content, amount);
            return amount;
        }

//        log.info("Response of AmountUtils getAmount final content :{} => result :{}", content, BigDecimal.ZERO);
        return BigDecimal.ZERO;
    }

    /**
     * 提取数字金额文本
     *
     * @param content 文本
     * @return
     */
    private static String getNumberAmountContent(String content) {
        String newContent = StringUtils.replace(content, " ", "");
        Matcher matcher = numberAmountRegx.matcher(newContent);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 数字金额转为具体金额
     *
     * @param numberAmount 数字金额字符串
     * @return
     */
    private static BigDecimal numberAmount2Number(String numberAmount) {
//        log.info("Request to AmountUtils numberAmount2Number numberAmount :{}", numberAmount);

        BigDecimal amount = BigDecimal.ZERO;

        if (StringUtils.isEmpty(numberAmount)) {
            return amount;
        }

        numberAmount = StringUtils.replace(numberAmount, ",", "");
        if (StringUtils.isEmpty(numberAmount)) {
            return amount;
        }

        amount = BigDecimal.valueOf(Double.parseDouble(removeUnit(numberAmount)));
        if (StringUtils.contains(numberAmount, "万")) {
            amount = changeW2Y(amount);
        }

//        log.info("Response of AmountUtils numberAmount2Number numberAmount2Number final result :{}", amount);
        return amount;
    }

    /**
     * 移除单位
     *
     * @param content
     * @return
     */
    private static String removeUnit(String content) {
        String[] regex = {"元", "万元", "万"};
        for (String value : regex) {
            content = StringUtils.replace(content, value, "");
        }
        return content;
    }

    /**
     * 万元转元
     *
     * @param amount
     * @return
     */
    private static BigDecimal changeW2Y(BigDecimal amount) {
        return amount.multiply(new BigDecimal(10000));
    }

    /**
     * 提取获取中文金额文本
     *
     * @param content 文本
     * @return
     */
    private static String getChineseAmountContent(String content) {
        String newContent = StringUtils.replace(content, " ", "");
        Matcher matcher = chineseAmountRegx.matcher(newContent);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 中文金额转具体数字金额
     *
     * @param chineseAmount 中文金额字符串
     * @return
     */
    public static BigDecimal chineseAmount2Number(String chineseAmount) {
//        log.info("Request to AmountUtils chineseAmount2Number chineseAmount :{}", chineseAmount);
        chineseAmount = chineseAmount.replace("元", "圆");
        chineseAmount = chineseAmount.replace("正", "整");

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal num = BigDecimal.ZERO;
        for (int i = 0; i < chineseAmount.length(); i++) {
            // 判断是否是金额单位
            boolean isUnit = true;

            char c = chineseAmount.charAt(i);
            for (int j = 0; j < numArray.length; j++) {
                // 非单位，即数字
                if (c == numArray[j]) {
                    num = BigDecimal.valueOf(j);
                    isUnit = false;
                    break;
                }
            }

            // 如果是单位字符
            if (isUnit) {

                // 第一个就是单位，如：拾伍万圆整
                if (i == 0) {
                    num = BigDecimal.ONE;
                }

                // 获取当前单位字符后的最大单位字符的索引，以及单位值
                int maxUnitIndex = getMaxUnitIndexAfterCurrentUnitChar(i, chineseAmount);
                BigDecimal maxUnit = getUnitByUnitIndex(maxUnitIndex);

                // 获取当前单位字符的索引，以及单位值
                int currentUnitIndex = getUnitIndex(c);
                BigDecimal currentUnit = getUnit(c);

                // 计算当前数值的值
                BigDecimal temp = num.multiply(currentUnit);

                // 当前单位不是大单位，则需要乘最大单位值
                if (currentUnitIndex != maxUnitIndex) {
                    temp = temp.multiply(maxUnit);
                }

                // 做加法
                result = result.add(temp);
                num = BigDecimal.ZERO;
            }
        }

//        log.info("Response of AmountUtils numberAmount2Number numberAmount2Number final result :{}", result);
        return result;
    }

    /**
     * 获取当前单位字符后的最大单位
     *
     * @param currentIndex  当前字符索引
     * @param chineseAmount 中文金额字符串
     * @return
     */
    private static int getMaxUnitIndexAfterCurrentUnitChar(int currentIndex, String chineseAmount) {
        int maxUnitIndex = 0;
        for (int i = currentIndex; i < chineseAmount.length(); i++) {
            int unitIndex = getUnitIndex(chineseAmount.charAt(i));
            if (maxUnitIndex < unitIndex) {
                maxUnitIndex = unitIndex;
            }
        }
        return maxUnitIndex;
    }

    /**
     * 获取单位字符的索引
     *
     * @param unitChar 单位字符
     * @return
     */
    private static int getUnitIndex(char unitChar) {
        for (int j = 0; j < unitArray.length; j++) {
            if (unitChar == unitArray[j]) {
                return j;
            }
        }
        return 0;
    }

    /**
     * 获取单位字符对应的单位值
     * <p>
     * 单位字符 {'整', '厘', '分', '角', '圆', '拾', '佰', '仟', '万', '亿'}
     *
     * @param unitChar 单位字符
     * @return
     */
    private static BigDecimal getUnit(char unitChar) {
        int unitIndex = getUnitIndex(unitChar);
        return getUnitByUnitIndex(unitIndex);
    }

    /**
     * 根据单位索引获取对应的单位值
     * <p>
     * 单位字符 {'整', '厘', '分', '角', '圆', '拾', '佰', '仟', '万', '亿'}
     *
     * @param unitIndex 单位索引
     * @return
     */
    private static BigDecimal getUnitByUnitIndex(int unitIndex) {
        BigDecimal num = BigDecimal.ZERO;
        switch (unitIndex) {
            case 0:
                num = BigDecimal.valueOf(0);
                break;
            case 1:
                num = BigDecimal.valueOf(0.001);
                break;
            case 2:
                num = BigDecimal.valueOf(0.01);
                break;
            case 3:
                num = BigDecimal.valueOf(0.1);
                break;
            case 4:
                num = BigDecimal.valueOf(1);
                break;
            case 5:
                num = BigDecimal.valueOf(10);
                break;
            case 6:
                num = BigDecimal.valueOf(100);
                break;
            case 7:
                num = BigDecimal.valueOf(1000);
                break;
            case 8:
                num = BigDecimal.valueOf(10000);
                break;
            case 9:
                num = BigDecimal.valueOf(100000000);
                break;
            default:
                break;
        }
        return num;
    }


    /**
     * 金额转成中文大写金额
     * <p>
     *
     * @param amount
     * @return
     */
    public static String numberAmount2ChineseAmount(BigDecimal amount) {
        //  -1, 0, or 1 as the value of this {@code BigDecimal} is negative, zero, or positive.
        int sigNum = amount.signum();

        // 零元整的情况
        if (sigNum == 0) {
            return "零元整";
        }

        // 保留3位小数，大写金额最小是厘
        String s = new DecimalFormat("##0.000").format(amount);

        // 以小数点为界分割这个字符串，获取小数和整数部分
        int index = s.indexOf(".");
        String intStr = s.substring(0, index);
        String smallStr = s.substring(index + 1);

        String result = number2Chinese(intStr, 1) + number2Chinese(smallStr, 2);

        // 清理多余的零
        result = cleanZero(result);

        // 如果最后一个单位是元，则添加整
        if (result.substring(result.length() - 1).equals("元")) {
            result = result + "整";
        }

        if (sigNum == -1) {
            return "负" + result; // 如果是负数，加上"负"
        } else {
            return result;
        }
    }


    /**
     * 将整数部分转为大写的金额
     *
     * @param s    数字金额字符串
     * @param flag 1 整数部分，2小数部分
     * @return
     */
    private static String number2Chinese(String s, int flag) {

        String newString = String.valueOf(Long.parseLong(s));

        // 金额大写数组
        String[] numArray = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

        // 整数部分单位
        String[] intUnitArray = {
                // 元到万
                "元", "拾", "佰", "仟", "万",
                // 拾万位到仟万位
                "拾", "佰", "仟",
                // 亿位到万亿位
                "亿", "拾", "佰", "仟", "万"};

        // 小数部单位
        String[] smallUnitArray = {"角", "分", "厘"};

        // 用来存放转换后的新字符串
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < newString.length(); i++) {
            // 根据数字对应的坐标，获取对应的大写金额
            int index = Integer.parseInt(String.valueOf(newString.charAt(i)));
            if (flag == 1) {
                // 转换整数部分为中文大写形式（带单位）
                stringBuilder.append(numArray[index]).append(intUnitArray[s.length() - i - 1]);
            } else if (flag == 2) {
                // 转换小数部分（带单位）
                stringBuilder.append(numArray[index]).append(smallUnitArray[i]);
            }

        }
        return stringBuilder.toString();
    }

    /**
     * 清理大写金额中多余的零
     *
     * @param s String 已经转换好的字符串
     * @return 改进后的字符串
     */
    private static String cleanZero(String s) {

        // 字符串中存在多个'零'在一起的时候只读出一个'零'，并省略多余的单位
        String[] regex1 = {"零仟", "零佰", "零拾"};
        String[] regex2 = {"零亿", "零万", "零元"};
        String[] regex3 = {"亿", "万", "元"};
        String[] regex4 = {"零角", "零分", "零厘"};

        // 第一轮转换把 "零仟", 零佰","零拾"等字符串替换成一个"零"
        for (String value : regex1) {
            s = s.replaceAll(value, "零");
        }

        // 第二轮转换考虑 "零亿","零万","零元"等情况
        // "亿","万","元"这些单位有些情况是不能省的，需要保留下来
        for (int i = 0; i < 3; i++) {
            // 当第一轮转换过后有可能有很多个零叠在一起，要把很多个重复的零变成一个零
            s = s.replaceAll("零零零", "零");
            s = s.replaceAll("零零", "零");
            s = s.replaceAll(regex2[i], regex3[i]);
        }

        // 第三轮转换把"零角","零分","零厘" 字符串省略
        for (String value : regex4) {
            s = s.replaceAll(value, "");
        }

        // 当"万"到"亿"之间全部是"零"的时候，忽略"亿万"单位，只保留一个"亿"
        s = s.replaceAll("亿万", "亿");
        return s;
    }


    public static void main(String[] args) {

        String n1 = "10000";
        String n2 = "10000元";
        String n3 = "1万";
        String n4 = "1万元";
        String n5 = "我有1元";
        String n6 = "我有元";
        String n7 = "我有1万元哈哈";
        String n8 = "已履行行政处罚决定,罚款10000元哈哈";

        System.out.printf("%s = %s\n", n1, AmountUtils.getAmount(n1));
        System.out.printf("%s = %s\n", n2, AmountUtils.getAmount(n2));
        System.out.printf("%s = %s\n", n3, AmountUtils.getAmount(n3));
        System.out.printf("%s = %s\n", n4, AmountUtils.getAmount(n4));
        System.out.printf("%s = %s\n", n5, AmountUtils.getAmount(n5));
        System.out.printf("%s = %s\n", n6, AmountUtils.getAmount(n6));
        System.out.printf("%s = %s\n", n7, AmountUtils.getAmount(n7));
        System.out.printf("%s = %s\n", n8, AmountUtils.getAmount(n8));


        String s1 = "定给予当事人处以罚款人民币陆拾贰万贰仟玖佰壹拾玖元肆角的行政处罚";
        String s2 = "壹万伍仟肆佰壹拾圆叁角伍分肆厘";
        String s3 = "捌万陆仟肆佰壹拾圆整";
        String s4 = "壹万伍仟肆佰壹拾元贰角捌分肆厘";
        String s5 = "拾壹亿壹仟万伍仟肆佰壹拾元贰角捌分肆厘";
        String s6 = "壹佰壹拾万元整";
        String s7 = "整当事人的行为，xx成分，监督检查部门应当责令停止违法行为，消除影响，可以根据情节处以一万元以上二十万元以下的罚款”之规定。我局决定对当事人作出如下行政处罚：\\\\n1、责令停止发布违规广告。\\\\n2、罚没合计:壹佰壹拾万元整。";

        System.out.printf("%s = %s\n", s1, AmountUtils.getAmount(s1));
        System.out.printf("%s = %s\n", s2, AmountUtils.getAmount(s2));
        System.out.printf("%s = %s\n", s3, AmountUtils.getAmount(s3));
        System.out.printf("%s = %s\n", s4, AmountUtils.getAmount(s4));
        System.out.printf("%s = %s\n", s5, AmountUtils.getAmount(s5));
        System.out.printf("%s = %s\n", s6, AmountUtils.getAmount(s6));
        System.out.printf("%s = %s\n", s7, AmountUtils.getAmount(s7));

        BigDecimal b1 = BigDecimal.valueOf(10000);
        BigDecimal b2 = BigDecimal.valueOf(100000000001.1);
        BigDecimal b3 = BigDecimal.valueOf(10001.1034);
        BigDecimal b4 = BigDecimal.valueOf(10000.2345);
        BigDecimal b5 = BigDecimal.valueOf(10000.200);
        BigDecimal b6 = BigDecimal.valueOf(10000.0);
        BigDecimal b7 = BigDecimal.valueOf(86410);
        BigDecimal b8 = BigDecimal.valueOf(0.00);

        System.out.printf("%s = %s\n", b1, AmountUtils.numberAmount2ChineseAmount(b1));
        System.out.printf("%s = %s\n", b2, AmountUtils.numberAmount2ChineseAmount(b2));
        System.out.printf("%s = %s\n", b3, AmountUtils.numberAmount2ChineseAmount(b3));
        System.out.printf("%s = %s\n", b4, AmountUtils.numberAmount2ChineseAmount(b4));
        System.out.printf("%s = %s\n", b5, AmountUtils.numberAmount2ChineseAmount(b5));
        System.out.printf("%s = %s\n", b6, AmountUtils.numberAmount2ChineseAmount(b6));
        System.out.printf("%s = %s\n", b7, AmountUtils.numberAmount2ChineseAmount(b7));
        System.out.printf("%s = %s\n", b8, AmountUtils.numberAmount2ChineseAmount(b8));

    }
}