JaCoCo Java Code Coverage Library (v0.8.7)
=================================

[![Build Status](https://dev.azure.com/jacoco-org/JaCoCo/_apis/build/status/JaCoCo?branchName=master)](https://dev.azure.com/jacoco-org/JaCoCo/_build/latest?definitionId=1&branchName=master)
[![Build status](https://ci.appveyor.com/api/projects/status/g28egytv4tb898d7/branch/master?svg=true)](https://ci.appveyor.com/project/JaCoCo/jacoco/branch/master)
[![Maven Central](https://img.shields.io/maven-central/v/org.jacoco/jacoco.svg)](http://search.maven.org/#search|ga|1|g%3Aorg.jacoco)

JaCoCo is a free Java code coverage library distributed under the Eclipse Public
License. Check the [project homepage](http://www.jacoco.org/jacoco)
for downloads, documentation and feedback.

Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco)
for questions regarding JaCoCo which are not already covered by the
[extensive documentation](http://www.jacoco.org/jacoco/trunk/doc/).

Note: We do not answer general questions in the project's issue tracker. Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco) for this.
-------------------------------------------------------------------------
## 源码变动说明
- 父module在构建时去除了[org.jacoco.agent.rt,org.jacoco.examples jacoco-maven-plugin,org.jacoco.tests,org.jacoco.doc] 这些module的依赖。
- 只启用了org.jacoco.cli这个模块，以及这个模块依赖的相关模块
## 使用方式
```shell script
## 首先从服务器 dump exec文件
java -jar org.jacoco.cli-0.8.7-nodeps.jar dump \
  --address 127.0.0.1 \
  --port 6300 \
  --destfile  /your/exec/file/path/xxx.exec
## 然后生成报告
java -jar org.jacoco.cli-0.8.7-nodeps.jar \
  report /your/exec/file/path/xxx.exec \
  --classfiles /your/java/classes/path/.../classes \
  --sourcefiles /your/java/src/path/.../main/java \
  --html /your/output/dir/path \
  --gitRepositoryPath /your/gitrepository/path \ 
  --fromCommit master \
  --toCommit develop
```
- --gitremoteurl 要使用http的方式
- [gitRepositoryPath,fromCommit,gitremoteurl,toCommit] 这些参数均填写正确才会触发增量代码模式，否则时全量模式