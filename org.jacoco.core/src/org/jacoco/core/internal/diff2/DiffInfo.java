package org.jacoco.core.internal.diff2;

import lombok.Data;

import java.util.List;

enum DiffType{
    NEW("new"), MODIFY("modify"),DELETED("deleted"),RENAMED("renamed"),EMPTY("empty");

    private final String diffType;

    DiffType(String diffType) {
        this.diffType = diffType;
    }
}

@Data
public class DiffInfo {
    private DiffType type;
    private String file_path;
    private List<Integer> lines;
}
