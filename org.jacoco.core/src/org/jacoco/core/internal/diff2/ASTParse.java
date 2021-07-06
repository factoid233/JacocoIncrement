package org.jacoco.core.internal.diff2;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ASTParse {

    private final String java_text;
    private final String source_path;
    private CompilationUnit astRoot;

    public ASTParse(String source_path, String java_text){
        this.source_path = source_path;
        this.java_text = java_text;
        this.initCompilationUnit();
    }
    private void initCompilationUnit() {
        //  AST初始化 编译
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setBindingsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        char[] source = this.java_text != null ?this.java_text.toCharArray() : new char[0];
        parser.setSource(source);
        parser.setUnitName(this.source_path);
        astRoot = (CompilationUnit) parser.createAST(null);
    }

    /**
     * 解析方法参数
     * @param parameters
     * @return
     */
    private List<ArgInfo> parse_code_method_params(List parameters){
        List<ArgInfo> argInfos = new ArrayList<>();
        parameters.forEach(parameter->{
            ArgInfo argInfo = new ArgInfo();
            SingleVariableDeclaration parameter1 = (SingleVariableDeclaration) parameter;
            argInfo.setArgName(parameter1.getName().toString());
            argInfo.setType(parameter1.getType().toString());
            argInfos.add(argInfo);
        });
        return argInfos;
    }
    /**
     * 解析方法
     * @param methods
     * @return
     */
    private List<MethodInfo> parse_code_method(MethodDeclaration[] methods){
        List<MethodInfo> methodInfos = new ArrayList<>();
        for (MethodDeclaration method:methods){
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setMethodName(method.getName().toString());
            // 获取参数
            List parameters = method.parameters();
            methodInfo.setArgs(parse_code_method_params(parameters));

            //获取行数
            int start_line = astRoot.getLineNumber(method.getStartPosition());
            int end_line = astRoot.getLineNumber(method.getStartPosition()+method.getLength());
            List<Integer> lines = new ArrayList<>();
            for (int i = start_line; i <= end_line ; i++) lines.add(i);
            methodInfo.setLines(lines);

            methodInfos.add(methodInfo);
        }
        return methodInfos;
    }

    /**
     * 解析类信息
     * @return
     */
    public List<ClassInfo> parse_code_class(){
        List<ClassInfo> class_info_list =  new ArrayList<>();
        for (Object type : astRoot.types()) {
            TypeDeclaration type1  = (TypeDeclaration) type;
            ClassInfo classInfo = new ClassInfo();
            //解析包名
            classInfo.setPackages(astRoot.getPackage().getName().toString());
            //解析类名
            classInfo.setClassName(type1.getName().toString());
            //解析每个类的行数
            int start_line = astRoot.getLineNumber(type1.getStartPosition());
            int end_line = astRoot.getLineNumber(type1.getStartPosition()+type1.getLength());
            List<Integer> lines =  new ArrayList<>();
            for (int i = start_line; i <= end_line ; i++) {
                lines.add(i);
            }
            classInfo.setLines(lines);

            //解析方法
            List<MethodInfo> methodInfos = parse_code_method(type1.getMethods());
            classInfo.setMethodInfos(methodInfos);

            // 增加到数组中
            class_info_list.add(classInfo);
        }
        return class_info_list;
    }

}
