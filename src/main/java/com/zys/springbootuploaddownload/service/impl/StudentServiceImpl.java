package com.zys.springbootuploaddownload.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.zys.springbootuploaddownload.dao.StudentDao;
import com.zys.springbootuploaddownload.entity.Student;
import com.zys.springbootuploaddownload.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zhongyushi
 * @date 2020/9/29 0029
 * @dec 描述
 */
@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDao studentDao;

    @Override
    public int importExcel(JSONArray jsonArray) {
        return studentDao.importExcel(jsonArray);
    }

    @Override
    public List<Student> queryList() {
        return studentDao.queryList();
    }
}
