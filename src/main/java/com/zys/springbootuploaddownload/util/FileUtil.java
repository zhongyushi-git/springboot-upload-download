package com.zys.springbootuploaddownload.util;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author zhongyushi
 * @date 2020/9/29 0029
 * @dec 文件工具类，用于上传和下载
 */
public class FileUtil {


    public static String uploadFile(MultipartFile file,String uploadPath) {
        //设置日期的格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        //设置文件的保存路径是项目运行目录下的uploadFile目录下
        String realPath = new File(uploadPath).getAbsolutePath();
        //通过日期 对文件归类，如2019/11/30,2019/11/29
        String format = File.separator + sdf.format(new Date());
        format = format.replace("/", "\\");
        //根据规则创建目录
        File folder = new File(realPath + format);
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        //获取文件的原始名
        String oldName = file.getOriginalFilename();
        //获取文件的后缀名
        String suffix = oldName.substring(oldName.lastIndexOf("."));
        //使用uuid设置新的文件名，防止文件名重复
        String newName = UUID.randomUUID().toString() + suffix;
        try {
            //文件保存
            file.transferTo(new File(folder, newName));
            //生成文件的保存路径
            String accessPath = realPath + format + File.separator + newName;
            return accessPath;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 指定路径下载文件
     * @param path 文件路径
     * @param filename 文件名，带后缀
     * @param response
     * @throws IOException
     */
    public static void downloadFile(String path, String filename, HttpServletResponse response) throws IOException {
        File file = new File(path + File.separator + filename);
        if (!file.exists()) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("error");
            return;
        }
        FileInputStream in = new FileInputStream(file);
        downloadFile(filename, in, response);
    }

    /**
     * 通过流下载文件
     *
     * @param filename 文件名，带后缀
     * @param in 输入流
     * @param response
     * @throws IOException
     */
    public static void downloadFile(String filename, InputStream in, HttpServletResponse response) throws IOException {
        //设置浏览器直接下载文件，不打开，并设置文件名的编码
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        int len = 0;
        byte bytes[] = new byte[1024];
        OutputStream out = response.getOutputStream();
        while ((len = in.read(bytes)) > 0) {
            out.write(bytes, 0, len);
        }
        in.close();
    }
}
