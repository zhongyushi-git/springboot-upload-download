package com.zys.springbootuploaddownload.service;

import com.alibaba.fastjson.JSONArray;
import com.zys.springbootuploaddownload.entity.Student;

import java.util.List;
import java.util.Map;

/**
 * @author zhongyushi
 * @date 2020/9/29 0029
 * @dec 描述
 */
public interface StudentService {
    int importExcel(JSONArray jsonArray);

    List<Student> queryList();
}
