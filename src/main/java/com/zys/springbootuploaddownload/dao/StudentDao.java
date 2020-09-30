package com.zys.springbootuploaddownload.dao;

import com.alibaba.fastjson.JSONArray;
import com.zys.springbootuploaddownload.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zhongyushi
 * @date 2020/9/29 0029
 * @dec 描述
 */
@Mapper
public interface StudentDao {
    int importExcel(JSONArray jsonArray);

    List<Student> queryList();

}
