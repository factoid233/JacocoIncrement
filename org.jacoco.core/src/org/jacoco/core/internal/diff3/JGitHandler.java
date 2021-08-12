package org.jacoco.core.internal.diff3;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.jacoco.core.internal.diff3.dto.DiffEntryDto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JGitHandler {

    private final File gitRepositoryPath;
    private Git git;
    private Repository repository;
    private RevWalk walk;

    public JGitHandler(String gitRepositoryPath){
        this.gitRepositoryPath = new File(gitRepositoryPath);
        gitInit();
    }
    private void gitInit(){
        try {
            git = Git.open(gitRepositoryPath);
            repository = git.getRepository();
            walk = new RevWalk(repository);
        } catch (IOException e) {
            throw new RuntimeException("本地git仓库打开失败");
        }

    }
    public void gitCheckout(String branchName){
        try {
            git.checkout().setName(branchName).call();
        } catch (GitAPIException e) {
            throw new RuntimeException("检出分支 " + branchName + "异常\n" + e.getMessage());
        }
    }

    /**
     * 准备TreeIter
     * @param branchName 分支名称或者commitid
     * @return
     */
    private CanonicalTreeParser prepare_tree_parser(String branchName, ObjectReader reader) {
        try {
            ObjectId head = repository.resolve(branchName);
            RevTree tree = walk.parseTree(head);
            CanonicalTreeParser TreeIter = new CanonicalTreeParser();
            TreeIter.reset(reader,tree.getId());
            return TreeIter;
        } catch (IOException e) {
            throw new RuntimeException("解析Head "+ branchName + "\t出错"+ "\n" + e.getMessage());
        }

    }
    private String parseFilePath(String text){
        Pattern pattern = Pattern.compile("\\+\\+\\+ b/(.+)\r?\n");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()){
            String relativePath = matcher.group(1);
            return gitRepositoryPath.getPath() + File.separator + relativePath;
        }else {
            // 删除文件 /dev/null  返回null
            return null;
        }
    }
    private List<Integer> parseDiffLines(String text){
        Pattern pattern = Pattern.compile("@@(.+?)@@");
        Matcher matcher = pattern.matcher(text);
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

    private List<DiffEntryDto> parseAllDiffContent(String text){
        String[] text1 = text.split("diff --git\\s+a/.+b/.+\r?\n");
        List<String> text2 = Arrays.asList(text1);
        // 过滤掉空值
        List<String> text3 = text2.stream()
                .filter(s -> s.length() != 0)
                .collect(Collectors.toList());
        // 格式化DTO
        List<DiffEntryDto> diffEntryDtoList = new ArrayList<>();
        for (String str:text3){
            DiffEntryDto diffEntryDto = new DiffEntryDto();
            String filePath = parseFilePath(str);
            // 可能是删除文件跳过
            if (filePath == null)  continue;
            diffEntryDto.setFilePath(filePath);
            diffEntryDto.setLines(parseDiffLines(str));
            diffEntryDto.setDiffContent(str);
            diffEntryDtoList.add(diffEntryDto);
        }
        return diffEntryDtoList;
    }
    public List<DiffEntryDto> get_diff(String oldHead, String newHead){
        Repository repo = git.getRepository();
        try {
            // Instanciate a reader to read the data from the Git database
            ObjectReader reader = repo.newObjectReader();
            // 存储diff内容
            OutputStream out = new ByteArrayOutputStream();
            List<DiffEntry> listDiffs = git.diff()
                    .setOldTree(prepare_tree_parser(oldHead,reader))
                    .setNewTree(prepare_tree_parser(newHead,reader))
                    .setOutputStream(out)
                    .call();
            List<DiffEntryDto> results = parseAllDiffContent(out.toString());
            return results;
        } catch (GitAPIException e) {
            throw new RuntimeException("调用git diff 出错" + "\n" + e.getMessage());
        }
    }
    public String getFileContent(String filePath){
        try {
            Path path = Paths.get(filePath);
            byte[] data = Files.readAllBytes(path);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    throw new RuntimeException("文件"+filePath+"读取失败");
    }

}
