package org.jacoco.core.internal.diff3.dto;
import lombok.Data;

import java.util.List;

@Data
public class DiffEntryDto {
    private String filePath;
    private ChangeType changeType;
    private String diffContent;
    private List<Integer> lines;
    public static enum ChangeType {
        ADD,
        MODIFY,
        DELETE,
        RENAME,
        COPY;

        private ChangeType() {
        }
    }
}
