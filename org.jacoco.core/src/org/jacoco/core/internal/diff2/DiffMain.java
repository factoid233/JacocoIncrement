package org.jacoco.core.internal.diff2;

import com.alibaba.fastjson.JSONArray;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class DiffMain {
    private final String from_commit;
    private final String to_commit;
    private final GitlabHandler git;
    private final Map<String,List<MethodInfo>> diff_class_exist_method = new HashMap<>();

    public DiffMain(String host, String project_id, String token, String from_commit, String to_commit){
        this.from_commit = from_commit;
        this.to_commit = to_commit;
        this.git = new GitlabHandler(host,project_id,token);
    }
    public Map<String, List<MethodInfo>> run_diff(){
        JSONArray diff_src = git.get_commit_diff(from_commit,to_commit);
        List<DiffInfo> diff = DiffParse.parse(diff_src);
        filter(diff);
        parse_diff_method(diff);
        return diff_class_exist_method;
    }

    /**
     * 过滤类型为 空白的、 删除的 diff
     * 过滤不是java结尾的diff
     * @param data
     */
    private void filter(List<DiffInfo> data){
        data.removeIf(file -> file.getType() == DiffType.DELETED || file.getType() == DiffType.EMPTY);
        data.removeIf(file->!file.getFile_path().endsWith(".java"));
    }

    /**
     * 将diff出的行数转换成方法
     * @param data
     */
    private void parse_diff_method(List<DiffInfo> data){
        data.forEach(item ->{
            String file_path = item.getFile_path();
            String java_text = git.get_file_content(file_path, to_commit);
            ASTParse ast_parse = new ASTParse(file_path,java_text);
            List<Integer> diff_lines = item.getLines();
            List<ClassInfo> diff_tree = ast_parse.parse_code_class();
            lines_to_method(diff_tree,diff_lines);
        });
    }

    /**
     * 将行和方法对应上
     * @param diff_tree
     * @param diff_lines
     */
    private void lines_to_method(List<ClassInfo> diff_tree, List<Integer> diff_lines){
        diff_tree.forEach(classInfo->{
            List<MethodInfo> diff_exist_method = new ArrayList<>();
            //拼接处jacoco visitmethod方法中的name 后续可以直接根据Map的key取到其方法值
            String name = classInfo.getPackages().replace(".","/") + "/" + classInfo.getClassName();
            classInfo.getMethodInfos().forEach(methodInfo -> {
                List<Integer> method_lines = methodInfo.getLines();
                Set<Integer> method_lines_set = new HashSet<>(method_lines);
                method_lines_set.retainAll(diff_lines);
                if (!method_lines_set.isEmpty()){
                    diff_exist_method.add(methodInfo);
                }
            });
            diff_class_exist_method.put(name,diff_exist_method);
        });
    }
    public static Boolean is_contain_method(String location, String current_method,String current_method_args,Map<String, List<MethodInfo>> diffs){
        if (diffs == null){
            //如果diffs为null走全量覆盖率
            return true;
        }
        if (diffs.containsKey(location)){
            List<MethodInfo> methods = diffs.get(location);
            for (MethodInfo method:methods){
                // 判断方法是否在diff 类中 选择方法
                if (current_method.equals(method.getMethodName())){
                    return checkArgs(current_method_args,method.getArgs());
                }
            }
        }
    return false;
    }

    /**
     * 判断参数是否相同，主要通过参数类型以及个数判断
     * 暂未对返回类型做校验判断，后期可优化
     * @param current_method_args_src
     * @param reference_args
     * @return
     */
    private static Boolean checkArgs(String current_method_args_src,List<ArgInfo> reference_args){
        Type[] current_method_args = Type.getArgumentTypes(current_method_args_src);
        //判断参数个数是否为空
        if (current_method_args.length ==0 && reference_args.size() ==0){
            return true;
        }
        if (current_method_args.length == reference_args.size()){
            //判断参数类型是否相同
            List<Boolean> is_same_list = new ArrayList<>();
            for (int i=0;i<current_method_args.length;i++){
                Type current_method_arg = current_method_args[i];
                String current_method_arg1 = current_method_arg.toString();
                String current_method_arg_final;
                String reference_arg_type = reference_args.get(i).getType();
                String reference_arg_type_final = reference_arg_type;
                // Ljava/lang/String;  / 分割 取出类名最后一个
                //替换jvm类型为正常类型
                if (type_map().containsKey(current_method_arg1)){
                    //如果 参数类型为jvm短标识
                    current_method_arg_final = type_map().get(current_method_arg1);
                }else {
                    //标记参数是不是数组
                    boolean is_array = current_method_arg1.contains("[");

                    String[] current_method_arg2 = current_method_arg1.split("/");
                    String current_method_arg3 = current_method_arg2[current_method_arg2.length - 1];
//                    String current_method_arg4 = current_method_arg3.replace(";","");
                    Pattern pattern = Pattern.compile("<.+>|;"); //去掉空格符合换行符
                    Matcher matcher = pattern.matcher(current_method_arg3);
                    String current_method_arg4 = matcher.replaceAll("");
                    reference_arg_type_final = pattern.matcher(reference_arg_type).replaceAll("");
                    // 暂不考虑二维数组
                    if (is_array) {
                        current_method_arg_final = current_method_arg4 + "[]";
                    }else {
                        current_method_arg_final = current_method_arg4;
                    }
                }
                is_same_list.add(current_method_arg_final.equals(reference_arg_type_final));
            }
            return is_same_list.stream().allMatch(f-> f);
        }
        return false;
    }

    /**
     * jvm 短类型标识转换
     * @return
     */
    private static Map<String,String> type_map(){
        Map<String,String> map = new HashMap<>();
        String[] longName = {"boolean","byte","char","short","int","long","float","double","int[]","float[]"};
        String[] shortName = {"Z","B","C","S","I","J","F","D","[I","[F"};
        IntStream.range(0, shortName.length).forEach(i->{
            map.put(shortName[i],longName[i]);
        });
    return map;
    }
}
