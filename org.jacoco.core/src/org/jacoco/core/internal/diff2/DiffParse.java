package org.jacoco.core.internal.diff2;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffParse {
    @NonNull
    public static List<DiffInfo> parse(JSONArray data_array){
        List<DiffInfo> diffInfos = new ArrayList<>();
        for (Object data:data_array){
            JSONObject data1 = (JSONObject) data;
            DiffInfo diffInfo = new DiffInfo();
            //设置diff文件地址
            diffInfo.setFile_path(data1.get("new_path").toString());
            // 设置diff类型
            diffInfo.setType(parseDiffType(data1));
            // 解析diff 行数
           diffInfo.setLines(parseDiffContent(data1.get("diff").toString()));
           diffInfos.add(diffInfo);
        }
        return diffInfos;
    }

    /**
     * 解析diff类型
     * @param data
     * @return
     */
    private static DiffType parseDiffType(JSONObject data){
        String[] type_list = new String[]{"renamed_file", "deleted_file", "new_file"};
        if (data.get("diff").toString().equals("")){
            return DiffType.EMPTY;
        }
        for ( String type:type_list){
            if (Boolean.TRUE.equals(data.get(type))){
                return DiffType.valueOf(type.replace("_file","").toUpperCase());
            }
        }
        return DiffType.MODIFY;
    }

    /**
     * 解析diff内容
     * @param data
     * @return diff变动的行数
     */
    private static List<Integer> parseDiffContent(String data){
        Pattern pattern = Pattern.compile("@@(.+?)@@");
        Matcher matcher = pattern.matcher(data);
        List<Integer> lines = new ArrayList<>();
        while (matcher.find()){
            String res1 = matcher.group();
            String[] res2 = res1.split("[,\\s]");
            int start = Integer.parseInt(res2[3]);
            int length = Integer.parseInt(res2[4].replace("+", ""));
            for (int i = start; i < start + length; i++) lines.add(i);
        }
        return lines;
    }
}
