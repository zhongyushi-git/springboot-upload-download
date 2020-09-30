package com.zys.springbootuploaddownload.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * excel表格实体
 */
@Data
public class ExcelData implements Serializable {

    private static final long serialVersionUID = 6133772627258154184L;

    private List<String> titles;//表头
    private List<List<Object>> rows;//表数据
    private String name;//页签名称

}
