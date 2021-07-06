package org.jacoco.core.internal.diff2;

import lombok.Data;

import java.util.List;

@Data
public class MethodInfo {
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 方法参数
     */
    private List<ArgInfo> args;
    /**
     * 行数范围
     */
    private List<Integer> lines;
}

@Data
class ArgInfo{
    public String argName;
    public String type;
}