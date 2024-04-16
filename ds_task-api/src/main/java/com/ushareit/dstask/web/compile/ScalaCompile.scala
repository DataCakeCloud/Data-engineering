package com.ushareit.dstask.web.compile

import java.io.{File, PrintWriter}

import org.apache.flink.util.FileUtils

import scala.tools.nsc.{Global, Settings}

class ScalaCompile extends CompileBase {

  def createSourceFile(className: String, classBody: String, classTargetDir: String): String = {
    val file = new File(new File(classTargetDir).getParent, className + ".scala")
    val out = new PrintWriter(file)
    out.print(classBody)
    out.close()
    file.getAbsolutePath
  }

  /**
   * 编译代码
   *
   * @param className
   * @param classBody
   * @param classPath
   * @param classTargetDir
   */
  override def compile(className: String, classBody: String, classPath: String, classTargetDir: String): Unit = {
    val settings = new Settings()
    settings.classpath.value = classPath
    settings.outdir.value = classTargetDir
    val g = new Global(settings)
    val run = new g.Run
    val sourceFile = createSourceFile(className, classBody, classTargetDir)
    val sourceFiles = List(sourceFile)

    run.compile(sourceFiles)
    FileUtils.deleteFileOrDirectory(new File(sourceFile))
  }
}
