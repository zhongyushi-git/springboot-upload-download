package com.zys.springbootuploaddownload.util;

import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author zhongyushi
 * @date 2020/9/29 0029
 * @dec 根据路径获取文件流
 */
@Component
public class GetFileStream {

    /**
     * 获取资源目录下的文件流
     *
     * @param path 文件路径
     * @param fileName 文件名，带后缀
     * @return
     */
    public InputStream getResourceStream(String path, String fileName) throws FileNotFoundException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path + File.separator + fileName);
        if (stream == null) {
            throw new FileNotFoundException("路径错误，未找到指定文件");
        }
        return stream;
    }


}
