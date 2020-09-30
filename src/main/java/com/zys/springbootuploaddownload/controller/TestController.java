package com.zys.springbootuploaddownload.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zys.springbootuploaddownload.entity.ExcelData;
import com.zys.springbootuploaddownload.entity.Student;
import com.zys.springbootuploaddownload.service.StudentService;
import com.zys.springbootuploaddownload.util.ExcelUtil;
import com.zys.springbootuploaddownload.util.FileUtil;
import com.zys.springbootuploaddownload.util.GetFileStream;
import com.zys.springbootuploaddownload.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhongyushi
 * @date 2020/9/29 0029
 * @dec 上传下载的接口
 */
@RestController
@Slf4j
public class TestController {

    @Value("uploadPath")
    private String uploadPath;

    @Value("importConfigName")
    private String importConfigName;

    @Autowired
    private GetFileStream getFileStream;

    @Autowired
    private StudentService studentService;


    /**
     * 文件上传，返回文件保存路径
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public String uploadFile(MultipartFile file) {
        String path = FileUtil.uploadFile(file, uploadPath);
        return path;
    }

    /**
     * 从本地下载文件
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/download1")
    public void download1(HttpServletResponse response) throws IOException {
        String path = "D:\\files";
        String fileName = "123.txt";
        FileUtil.downloadFile(path, fileName, response);
    }


    /**
     * 从资源目录下载文件
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/download2")
    public void download2(HttpServletResponse response) throws IOException {
        String path = "templates";
        String fileName = "学生信息导入模板.xls";
        InputStream stream = getFileStream.getResourceStream(path, fileName);
        FileUtil.downloadFile(fileName, stream, response);
    }

    /**
     * 导入学生信息
     *
     * @param file
     * @return
     */
    @PostMapping("/import")
    public JSONObject importExcel(MultipartFile file) {
        JSONObject jsonObject = new JSONObject();
        try {
            //获取配置文件的内容
            Map<String, String> keyMap = PropertiesUtil.getKeyAndValue(importConfigName, "student.");
            //上传文件
            String path = FileUtil.uploadFile(file, uploadPath);
            //读取上传的文件内容
            JSONArray jsonArray = ExcelUtil.readExcel(path, keyMap);
            int count = studentService.importExcel(jsonArray);
            if (count >= 0) {
                jsonObject.put("status", true);
                jsonObject.put("msg", "学生信息导入成功");
            } else {
                jsonObject.put("status", false);
                jsonObject.put("msg", "学生信息导入失败");
            }
        } catch (Exception e) {
            log.info("=====学生信息导入发生异常=====" + e.getMessage());
            jsonObject.put("status", false);
            jsonObject.put("msg", "学生信息导入失败");
        }
        return jsonObject;
    }

    /**
     * 学生信息导出
     *
     * @param response
     */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws Exception {
        List<Student> list = studentService.queryList();

        ExcelData data = new ExcelData();
        String fileName = "学生信息表";
        //设置工作表名称
        data.setName(fileName);
        //设置表头
        List<String> titles = new ArrayList();
        titles.add("姓名");
        titles.add("学号");
        titles.add("性别");
        titles.add("手机号");
        data.setTitles(titles);
        //设置数据内容
        List<List<Object>> rows = new ArrayList();
        for (Student stu : list) {
            List<Object> row = new ArrayList();
            row.add(stu.getName());
            row.add(stu.getSno());
            row.add(stu.getSex());
            row.add(stu.getPhone());
            rows.add(row);
        }
        data.setRows(rows);
        try {
            String formatName = new SimpleDateFormat("HHmmss").format(new Date());
            ExcelUtil.exportExcel(response, fileName + formatName, data);
        } catch (Exception e) {
            log.info("=====学生信息导出发生异常=====" + e.getMessage());
        }
    }

}
