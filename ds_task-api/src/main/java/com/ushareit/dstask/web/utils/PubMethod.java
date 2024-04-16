package com.ushareit.dstask.web.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ushareit.dstask.bean.CostMonitor;
import com.ushareit.dstask.common.vo.cost.CostResponseMapVo;
import com.ushareit.dstask.common.vo.cost.CostResponseVo;
import com.ushareit.dstask.constant.BaseConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.util.*;

public class PubMethod {

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    public static <T> Map<String, List<T>> listToListMap(String key, List<T> list) {
        Map<String, List<T>> map = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, PropertyDescriptor> propertyDescriptorMap = classToMap(list.get(0));
            PropertyDescriptor p = propertyDescriptorMap.get(key);
            for (T t : list) {
                try {
                    String properties = (String) (p.getReadMethod().invoke(t));
                    if (map.containsKey(properties)) {
                        map.get(properties).add(t);
                    } else {
                        map.put(properties, Lists.newArrayList(t));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    public static Double subtract(CostResponseVo a, CostResponseVo b) {
        Double x = 0d;
        Double y = 0d;
        if (a != null&&a.getJobNameQuantity()!=null) {
            x=a.getJobNameQuantity();
        }
        if (b != null&&b.getJobNameQuantity()!=null) {
            y=b.getJobNameQuantity();
        }
        return x-y;
    }

    public static String convertSqlWithList(String sql, List<String> ids) {
        Set<String> set = Sets.newHashSet();
        ids.forEach(s -> {
            set.add(s);
        });
        ids = Lists.newArrayList(set);
        String a = "(" + Joiner.on(",").join(ids) + ")";
        sql = String.format(sql, a);
        return sql;
    }

    public static String joinList(List<String> strings) {
        return Joiner.on(",").join(strings);
    }

    public static String joinList(Set<String> strings) {
        return Joiner.on(",").join(strings);
    }

    public static <T> List<List<T>> subList(List<T> list, int num) {
        List<List<T>> listList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(list)) {
            return listList;
        }
        if (num < 2) {
            listList.add(list);
            return listList;
        }
        if (list.size() <= num) {
            listList.add(list);
            return listList;
        }
        for (int i = 0; i < list.size(); i = i + num) {
            if (i + num < list.size()) {
                listList.add(list.subList(i, i + num));
            } else {
                listList.add(list.subList(i, list.size()));
            }
        }
        return listList;
    }


    public static String pkg(String preview, String platform) {
        String result = "";
        try {
            if ("android".equalsIgnoreCase(platform)) {
                int index = preview.indexOf("id=");
                if (index > -1) {
                    result = preview.substring(index + 3);
                    int index2 = result.indexOf("&");
                    if (index2 > -1) {
                        result = result.substring(0, index2);
                    }
                }
            }
            if ("ios".equalsIgnoreCase(platform)) {
                int index = preview.indexOf("id");
                if (index > -1) {
                    result = preview.substring(index);
                    int index2 = result.indexOf("?");
                    if (index2 > -1) {
                        result = result.substring(0, index2);
                    }
                }
            }
        } catch (Exception e) {

        }

        return result;
    }

    public static <T> Map<String, PropertyDescriptor> classToMap(T t) {
        Map<String, PropertyDescriptor> stringPropertyDescriptorMap = Maps.newHashMap();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(t.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (!key.equals("class")) {
                    stringPropertyDescriptorMap.put(key, property);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringPropertyDescriptorMap;
    }

    public static <T> void convertNull(T t) {
        Map<String, PropertyDescriptor> propertyDescriptorMap = classToMap(t);
        try {
            for (Map.Entry<String, PropertyDescriptor> entry : propertyDescriptorMap.entrySet()) {
                if (entry.getValue().getPropertyType() == String.class) {
                    String s = (String) (entry.getValue().getReadMethod().invoke(t, null));
                    if (StringUtils.isBlank(s)) {
                        entry.getValue().getWriteMethod().invoke(t, "");
                    }
                }

            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static double doubleScale2(Double d) {
        if (d != null) {
            BigDecimal bigDecimal = new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigDecimal.doubleValue();
        }
        return 0d;
    }

    public static <T> void printSql(T t) {
        List<String> treeSet = Lists.newArrayList(Sets.newTreeSet(PubMethod.classToMap(t).keySet()));
        StringBuffer stringBuffer = new StringBuffer("INSERT into ").append(t.getClass().getName()).append("(");
        StringBuffer buffer2 = new StringBuffer("(");
        for (String s : treeSet) {
            buffer2.append("#{ad.").append(s).append("},");
            for (int i = 0; i < s.length(); ++i) {
                if (Character.isUpperCase(s.charAt(i))) {
                    stringBuffer.append("_").append(s.substring(i, i + 1).toLowerCase());
                } else {
                    stringBuffer.append(s.substring(i, i + 1));
                }
            }
            stringBuffer.append(",");
        }
        buffer2.append(")");
        stringBuffer.append(") VALUES <foreach collection=\"list\" item=\"ad\" index=\"index\" separator=\",\"> ");
        stringBuffer.append(buffer2).append("</foreach>");
        System.out.println(stringBuffer.toString());
    }

    public static <T> void printTitle(T t) {
        List<String> treeSet = Lists.newArrayList(Sets.newTreeSet(PubMethod.classToMap(t).keySet()));
        StringBuffer title = new StringBuffer("<sql id=\"Base_Column\">");
        System.out.println(title.toString());
        StringBuffer stringBuffer = new StringBuffer("");
        for (String s : treeSet) {
            for (int i = 0; i < s.length(); ++i) {
                if (Character.isUpperCase(s.charAt(i))) {
                    stringBuffer.append("_").append(s.substring(i, i + 1).toLowerCase());
                } else {
                    stringBuffer.append(s.substring(i, i + 1));
                }
            }
            stringBuffer.append("").append(",");
        }
        System.out.println(stringBuffer.toString());
        System.out.println("</sql>");
    }

    public static <T> void printPropetiesSql(T t) {
        List<String> treeSet = Lists.newArrayList(Sets.newTreeSet(PubMethod.classToMap(t).keySet()));
        StringBuffer title = new StringBuffer("<resultMap id=\"BaseResultMap\" type=\"").append(t.getClass().getName()).append("\">");
        System.out.println(title.toString());
        for (String s : treeSet) {
            StringBuffer stringBuffer = new StringBuffer("<result column=\"");
            for (int i = 0; i < s.length(); ++i) {
                if (Character.isUpperCase(s.charAt(i))) {
                    stringBuffer.append("_").append(s.substring(i, i + 1).toLowerCase());
                } else {
                    stringBuffer.append(s.substring(i, i + 1));
                }
            }
            stringBuffer.append("\"  property=\"").append(s);
            stringBuffer.append("\" />");
            System.out.println(stringBuffer.toString());
        }
        System.out.println("</resultMap>");
    }

    public static List<CostResponseMapVo> convertCostMap(String name, List<CostResponseVo> costResponseVos) {
        List<CostResponseMapVo> costResponseMapVoList = Lists.newArrayList();
        Map<String, List<CostResponseVo>> map = listToListMap(name, costResponseVos);
        for (Map.Entry<String, List<CostResponseVo>> entry : map.entrySet()) {
            costResponseMapVoList.add(new CostResponseMapVo(entry.getKey(), entry.getValue()));
        }
        return costResponseMapVoList;
    }

    public static String resourceId(String resourceName) {
        return resourceName.substring(resourceName.lastIndexOf("/") + 1);
    }


    public static <T> PageInfo<T> getPageInfo(int currentPage, int pageSize, List<T> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        int total = list.size();
        if (total > pageSize) {
            int toIndex = pageSize * currentPage;
            if (toIndex > total) {
                toIndex = total;
            }
            list = list.subList(pageSize * (currentPage - 1), toIndex);
        }
        Page<T> page = new Page<>(currentPage, pageSize);
        page.addAll(list);
        page.setPages((total + pageSize - 1) / pageSize);
        page.setTotal(total);

        PageInfo<T> pageInfo = new PageInfo<>(page);
        return pageInfo;
    }

    public static boolean compareRatio(CostResponseVo costResponseVo, CostMonitor costmonitor) {
        if (costmonitor.getType() == 2) {
            if (BaseConstant.NODATA.equals(costResponseVo.getBasisRatio())) {
                return false;
            }
            Double cr = Double.valueOf(costResponseVo.getBasisRatio().substring(0, costResponseVo.getBasisRatio().length() - 1));
            Double cm = Double.valueOf(costmonitor.getRatio());
            return cr >= cm;
        } else {
            if (BaseConstant.NODATA.equals(costResponseVo.getRelativeRatio())) {
                return false;
            }
            Double cr = Double.valueOf(costResponseVo.getRelativeRatio().substring(0, costResponseVo.getRelativeRatio().length() - 1));
            Double cm = Double.valueOf(costmonitor.getRatio());
            return cr >= cm;
        }

    }

    public static Double percentToDouble(String percent) {
        if (StringUtils.isBlank(percent) || BaseConstant.NODATA.equals(percent)) {
            return 0d;
        }
        return Double.valueOf(percent.substring(0, percent.length() - 1));
    }

    public static int getDayOfWeek() {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w <= 0) {
            return 7;
        }
        return w;
    }

    public static boolean executeMonitor(CostMonitor costMonitor) {
        if (costMonitor.getFrep().contains("0")) {
            return true;
        }
        String currentDayOfWeek = String.valueOf(getDayOfWeek());
        if (costMonitor.getFrep().contains(currentDayOfWeek)) {
            return true;
        }
        return false;
    }

    public static String divHundred2Scale(Double num1, Double num2) {
        if (num1==null) {
            num1 = 0d;
        }
        if (num2==null) {
            num2 = 0d;
        }
        if (num2==0d){
            return "0%";
        }
        return new BigDecimal(num1).divide(new BigDecimal(num2), 4, BigDecimal.ROUND_HALF_UP).multiply(HUNDRED).setScale(2)+"%";
    }
}
