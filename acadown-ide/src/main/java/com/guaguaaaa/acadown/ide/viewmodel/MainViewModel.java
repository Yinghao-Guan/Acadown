package com.guaguaaaa.acadown.ide.viewmodel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.guaguaaaa.acadown.core.util.CslGenerator;
import com.guaguaaaa.acadown.core.ast.PandocNode;
import com.guaguaaaa.acadown.core.AcadownCompiler;
import com.guaguaaaa.acadown.core.api.CompilationResult;
import com.guaguaaaa.acadown.core.api.Diagnostic;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainViewModel {

    private final StringProperty inputContent = new SimpleStringProperty("");
    private final StringProperty generatedPdfPath = new SimpleStringProperty();

    private final StringProperty citationTemplate = new SimpleStringProperty();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");
    private final BooleanProperty isCompiling = new SimpleBooleanProperty(false);
    private final ObservableList<Diagnostic> diagnostics = FXCollections.observableArrayList();

    public StringProperty inputContentProperty() { return inputContent; }
    public StringProperty generatedPdfPathProperty() { return generatedPdfPath; }

    public StringProperty citationTemplateProperty() { return citationTemplate; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty isCompilingProperty() { return isCompiling; }
    public ObservableList<Diagnostic> getDiagnostics() { return diagnostics; }

    private String getPandocExecutable() {
        String pandocHome = System.getenv("PANDOC_HOME");
        if (pandocHome != null && !pandocHome.isEmpty()) {
            String separator = File.separator;
            if (!pandocHome.endsWith(separator)) {
                pandocHome += separator;
            }
            return pandocHome + "pandoc";
        }
        return "pandoc";
    }

    private File currentFile;

    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * 从文件加载内容到输入框
     */
    public void loadFile(File file) throws IOException {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        inputContent.set(content);
        this.currentFile = file;
    }

    /**
     * 将输入框内容保存到文件
     */
    public void saveFile(File file) throws IOException {
        Files.writeString(file.toPath(), inputContent.get(), StandardCharsets.UTF_8);
        this.currentFile = file;

        compilePdfInBackground(file);
    }

    /**
     * 后台 PDF 编译任务
     */
    private void compilePdfInBackground(File sourceFile) {
        if (isCompiling.get()) return;

        isCompiling.set(true);
        statusMessage.set("Compiling...");

        String acadownText = inputContent.get();
        if (acadownText == null || acadownText.isBlank()) {
            isCompiling.set(false);
            statusMessage.set("Skipped: Content is empty");
            return;
        }

        CompilationResult result = AcadownCompiler.compile(acadownText);

        Platform.runLater(() -> {
            diagnostics.setAll(result.diagnostics);
        });

        if (result.hasErrors()) {
            Platform.runLater(() -> {
                statusMessage.set("Syntax Error: " + result.diagnostics.get(0).message);
                isCompiling.set(false);
            });
            return;
        }

        String jsonOutput = result.pandocJson;

        Thread t = new Thread(() -> {
            try {
                String sourcePath = sourceFile.getAbsolutePath();
                String basePath = sourcePath.lastIndexOf(".") > 0 ?
                        sourcePath.substring(0, sourcePath.lastIndexOf(".")) :
                        sourcePath;
                File workDir = sourceFile.getParentFile();
                String pdfPath = basePath + ".pdf";
                String texPath = basePath + ".tex";

                File bibFile = getAssociatedBibFile();
                boolean useBib = bibFile.exists();
                File cslFile = new File("custom_style.csl");

                String userTemplate = citationTemplate.get();
                if (userTemplate == null || userTemplate.isBlank()) {
                    userTemplate = "{author} ({year}). {title}.";
                }
                String cslXml = CslGenerator.generateCslXml(userTemplate);
                Files.writeString(cslFile.toPath(), cslXml, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                // 1. Generate LaTeX (.tex)
                List<String> texCommand = new ArrayList<>();
                texCommand.add(getPandocExecutable());
                texCommand.add("-f"); texCommand.add("json");
                texCommand.add("-t"); texCommand.add("latex");
                texCommand.add("-s");
                texCommand.add("-o"); texCommand.add(texPath);

                if (useBib) {
                    texCommand.add("--citeproc");
                    texCommand.add("--bibliography"); texCommand.add(bibFile.getAbsolutePath());
                    texCommand.add("--csl"); texCommand.add(cslFile.getAbsolutePath());
                    texCommand.add("--metadata=link-bibliography=false");
                }

                runProcessWithInput(texCommand, jsonOutput, workDir);
                System.out.println("LaTeX file generated: " + texPath);

                // 2. Generate PDF (.pdf)
                List<String> pdfCommand = new ArrayList<>();
                pdfCommand.add(getPandocExecutable());
                pdfCommand.add("-f"); pdfCommand.add("json");
                pdfCommand.add("-t"); pdfCommand.add("pdf");
                pdfCommand.add("-o"); pdfCommand.add(pdfPath);
                pdfCommand.add("--pdf-engine=xelatex");
                // pdfCommand.add("-V"); pdfCommand.add("mainfont=Microsoft YaHei");

                if (useBib) {
                    pdfCommand.add("--citeproc");
                    pdfCommand.add("--bibliography"); pdfCommand.add(bibFile.getAbsolutePath());
                    pdfCommand.add("--csl"); pdfCommand.add(cslFile.getAbsolutePath());
                    pdfCommand.add("--metadata=link-bibliography=false");
                }

                ProcessExecutionResult pdfResult = runProcessWithInput(pdfCommand, jsonOutput, workDir);

                Platform.runLater(() -> {
                    isCompiling.set(false);
                    if (pdfResult.exitCode == 0) {
                        statusMessage.set("Saved: " + new File(pdfPath).getName() + " (& .tex)");
                        generatedPdfPath.set(null); // Force update if path is same
                        generatedPdfPath.set(pdfPath);
                    } else {
                        statusMessage.set("PDF Error (Code " + pdfResult.exitCode + ")");
                        System.err.println("Pandoc Error:\n" + pdfResult.errorOutput);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    isCompiling.set(false);
                    statusMessage.set("Error: " + e.getMessage());
                });
            }
        });

        t.setDaemon(true);
        t.start();
    }

    /**
     * 辅助类：保存进程执行结果
     */
    private static class ProcessExecutionResult {
        int exitCode;
        String errorOutput;

        public ProcessExecutionResult(int exitCode, String errorOutput) {
            this.exitCode = exitCode;
            this.errorOutput = errorOutput;
        }
    }

    /**
     * 辅助方法：安全地运行进程并写入标准输入，同时读取标准错误
     */
    private ProcessExecutionResult runProcessWithInput(
            List<String> command,
            String input
    ) throws IOException, InterruptedException {
        return runProcessWithInput(command, input, null);
    }

    private ProcessExecutionResult runProcessWithInput(List<String> command, String input, File workingDir)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(command);

        if (workingDir != null) {
            pb.directory(workingDir);
        }

        if (workingDir != null) {
            String key = "TEXINPUTS";
            String existing = pb.environment().getOrDefault(key, "");
            String sep = java.io.File.pathSeparator; // Windows 是 ;  Linux/mac 是 :
            String value = workingDir.getAbsolutePath() + sep + existing;
            pb.environment().put(key, value);
        }

        Process process = pb.start();

        // 写入 stdin
        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(input);
        }

        // 读取 stderr
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }

        // 读取并丢弃 stdout (防止缓冲区满导致挂起)
        try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (outputReader.readLine() != null) {
                // ignore
            }
        }

        int exitCode = process.waitFor();
        return new ProcessExecutionResult(exitCode, errorOutput.toString());
    }

    /**
     * 根据当前 Markdown 文件，推算同名的 .bib 文件路径
     */
    private File getAssociatedBibFile() {
        if (this.currentFile == null) {
            return new File("test.bib");
        }

        String mdPath = this.currentFile.getAbsolutePath();
        String bibPath;
        if (mdPath.lastIndexOf(".") > 0) {
            bibPath = mdPath.substring(0, mdPath.lastIndexOf(".")) + ".bib";
        } else {
            bibPath = mdPath + ".bib";
        }

        return new File(bibPath);
    }

    public void saveAsLatex(File outputFile) throws IOException, InterruptedException {
        String acadownText = inputContent.get();
        CompilationResult result = AcadownCompiler.compile(acadownText);

        if (result.hasErrors()) {
            throw new IOException("Syntax Error: " + result.diagnostics.get(0).message);
        }

        List<String> command = new ArrayList<>();
        command.add(getPandocExecutable());
        command.add("-f"); command.add("json");
        command.add("-t"); command.add("latex");
        command.add("-o"); command.add(outputFile.getAbsolutePath());

        runProcessWithInput(command, result.pandocJson);
    }
}