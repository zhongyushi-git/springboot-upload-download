package com.zys.springbootuploaddownload.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 读取.properties配置文件的内容至Map中
 */
public class PropertiesUtil {

    /**
     * 读取.properties配置文件的内容至Map
     *
     * @param propertiesFile
     * @return
     * @throws IOException
     */
    public static Map read(String propertiesFile) throws Exception {
        try {
            if (!(propertiesFile.indexOf("properties") > 0)) {
                propertiesFile = propertiesFile + ".properties";
            }
            InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile);
            Properties p = new Properties();
            p.load(inStream);

            Map<Object, Object> map = properties2map(p);
            return map;
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    /**
     * 将属性文件转为map
     *
     * @param prop
     * @return
     */
    public static Map properties2map(Properties prop) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        Enumeration enu = prop.keys();
        while (enu.hasMoreElements()) {
            Object obj = enu.nextElement();
            Object objv = prop.get(obj);
            map.put(obj, objv);
        }
        return map;
    }

    public static Map<String,String> getKeyAndValue(String fileName,String configName) throws Exception {
        Map<String,String> keyMap=new HashMap<>();
        Map<String, String> map = PropertiesUtil.read("importConfig.properties");
        //过滤文件内容，只截取student的配置
        for (String key : map.keySet()) {
            if (key.startsWith("student.")) {
                String[] split = key.split("student.");
                keyMap.put(map.get(key), split[1]);
            }
        }
        return keyMap;
    }
}
