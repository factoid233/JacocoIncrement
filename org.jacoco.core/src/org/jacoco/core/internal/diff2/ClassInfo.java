package org.jacoco.core.internal.diff2;

import lombok.Data;

import java.util.List;

@Data
public class ClassInfo {
    /**
     * java文件
     */
    private String classFile;
    /**
     * 类名
     */
    private String className;
    /**
     * 包名
     */
    private String packages;

    /**
     * 类中的方法
     */
    private List<MethodInfo> methodInfos;
    /**
     * 行数范围
     */
    private List<Integer> lines;

    /**
     * 新增的行数
     */
    private List<int[]> addLines;

    /**
     * 删除的行数
     */
    private List<int[]> delLines;

    /**
     * 修改类型
     */
    private String type;

}
