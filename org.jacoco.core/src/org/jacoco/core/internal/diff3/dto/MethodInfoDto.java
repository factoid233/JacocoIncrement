package org.jacoco.core.internal.diff3.dto;

import lombok.Data;

import java.util.List;

@Data
public class MethodInfoDto {
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 方法参数
     */
    private List<ArgInfoDto> args;
    /**
     * 行数范围
     */
    private List<Integer> lines;
}
