package com.guaguaaaa.acadown.ide.view;

import com.guaguaaaa.acadown.core.api.Diagnostic;
import com.guaguaaaa.acadown.ide.viewmodel.MainViewModel;
import com.guaguaaaa.acadown.ide.util.SyntaxHighlighter;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.scene.layout.StackPane;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.concurrent.Worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Base64;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import java.io.File;
import java.io.IOException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import netscape.javascript.JSObject;

public class MainView {

    @FXML private WebView previewWebView;
    @FXML private javafx.scene.control.TextField templateField;
    @FXML private javafx.scene.control.Label statusLabel;
    @FXML private javafx.scene.control.ProgressBar progressBar;
    @FXML private StackPane editorContainer;

    private CodeArea codeArea;
    private MainViewModel viewModel;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<Diagnostic> currentDiagnostics = new ArrayList<>();
    private final SyntaxHighlighter syntaxHighlighter = new SyntaxHighlighter();

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;

        this.codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStylesheets().add(getClass().getResource("editor.css").toExternalForm());
        codeArea.setStyle("-fx-font-family: 'Monospaced', 'Consolas', 'Courier New'; -fx-font-size: 14;");
        editorContainer.getChildren().add(codeArea);

        String content = this.viewModel.inputContentProperty().get();
        codeArea.replaceText(0, 0, content == null ? "" : content);

        this.viewModel.inputContentProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(codeArea.getText())) {
                codeArea.replaceText(newVal);
            }
        });

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            this.viewModel.inputContentProperty().set(newText);
            computeHighlightingAsync(newText);
        });

        viewModel.getDiagnostics().addListener((javafx.collections.ListChangeListener.Change<? extends Diagnostic> c) -> {
            this.currentDiagnostics = new ArrayList<>(viewModel.getDiagnostics());
            computeHighlightingAsync(codeArea.getText());
        });

        // 监听生成的 PDF 路径
        this.viewModel.generatedPdfPathProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;

            File pdfFile = new File(newVal);
            if (!pdfFile.exists()) return;

            try {
                byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
                String base64 = Base64.getEncoder().encodeToString(pdfBytes);

                Platform.runLater(() -> {
                    var engine = previewWebView.getEngine();

                    // 判断 viewer.html 是否已加载
                    String currentLoc = engine.getLocation();
                    boolean isAlreadyLoaded = currentLoc != null && currentLoc.endsWith("viewer.html");

                    // 1) 定义 updatePDF（不包含 base64，避免超长注入）
                    // 用 window.updatePDF 挂全局，防止作用域问题
                    String defineUpdatePdfFn =
                            "window.updatePDF = window.updatePDF || function(base64Data) {" +
                                    "  try {" +
                                    "    var pdfData = atob(base64Data);" +
                                    "    var uint8Array = new Uint8Array(pdfData.length);" +
                                    "    for (var i = 0; i < pdfData.length; i++) {" +
                                    "      uint8Array[i] = pdfData.charCodeAt(i);" +
                                    "    }" +
                                    "    if (window.PDFViewerApplication && window.PDFViewerApplication.open) {" +
                                    "      window.PDFViewerApplication.open({ data: uint8Array });" +
                                    "    }" +
                                    "  } catch(e) { console.error('Update Error: ' + e); }" +
                                    "};";

                    // 2) 分块注入 base64 到 window.__pdfBase64（避免 JavaFX executeScript 字符串长度上限）
                    Runnable injectBase64Chunks = () -> {
                        engine.executeScript("window.__pdfBase64 = '';");
                        int chunkSize = 100_000;
                        for (int i = 0; i < base64.length(); i += chunkSize) {
                            String chunk = base64.substring(i, Math.min(base64.length(), i + chunkSize));
                            engine.executeScript("window.__pdfBase64 += '" + chunk + "';");
                        }
                    };

                    // 3) 调用 updatePDF（使用 token 防抖：只执行最后一次）
                    // 每次更新都递增 token，避免旧的 interval 在后面“回放旧数据”
                    String token = Long.toString(System.currentTimeMillis());
                    engine.executeScript("window.__acadownPdfToken = '" + token + "';");

                    Runnable openPdfWhenReady = () -> {
                        // 轮询等待 PDF.js 初始化完成，然后再 open
                        // 只在 token 匹配时执行，确保不会被旧任务覆盖
                        String waitReady =
                                "var __t = window.__acadownPdfToken;" +
                                        "var __timer = setInterval(function() {" +
                                        "  if (window.__acadownPdfToken !== __t) { clearInterval(__timer); return; }" +
                                        "  if (window.PDFViewerApplication && window.PDFViewerApplication.open && window.updatePDF) {" +
                                        "    clearInterval(__timer);" +
                                        "    try {" +
                                        "      window.updatePDF(window.__pdfBase64);" +
                                        "      window.__pdfBase64 = '';" +
                                        "    } catch(e) { console.error('Open Error: ' + e); }" +
                                        "  }" +
                                        "}, 50);";
                        engine.executeScript(waitReady);
                    };

                    if (isAlreadyLoaded) {
                        // === 方案 A: 软更新 (Soft Update) ===
                        engine.executeScript(defineUpdatePdfFn);
                        injectBase64Chunks.run();
                        openPdfWhenReady.run();
                    } else {
                        // === 方案 B: 首次加载 (First Load) ===
                        var viewerResource = getClass().getResource("pdfjs/web/viewer.html");
                        if (viewerResource == null) return;

                        // 重要：避免重复添加 listener 导致多次触发
                        // 先 load，然后在 SUCCEEDED 时注入函数与数据
                        engine.load(viewerResource.toExternalForm());

                        engine.getLoadWorker().stateProperty().addListener((observable, oldState2, newState2) -> {
                            if (newState2 == Worker.State.SUCCEEDED) {
                                // 定义函数 + 注入数据 + 等 ready 再打开
                                engine.executeScript(defineUpdatePdfFn);
                                injectBase64Chunks.run();
                                openPdfWhenReady.run();
                            }
                        });
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        templateField.textProperty().bindBidirectional(this.viewModel.citationTemplateProperty());
        statusLabel.textProperty().bind(this.viewModel.statusMessageProperty());

        progressBar.visibleProperty().bind(this.viewModel.isCompilingProperty());
        progressBar.progressProperty().bind(
                javafx.beans.binding.Bindings.when(this.viewModel.isCompilingProperty())
                        .then(-1.0)
                        .otherwise(0.0)
        );
    }

    public static class JavaConsoleBridge {
        public void log(String text) {
            System.out.println("JS LOG: " + text);
        }
        public void error(String text) {
            System.err.println("JS ERROR: " + text);
        }
    }

    private void computeHighlightingAsync(String text) {
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return syntaxHighlighter.computeHighlighting(text);
            }
        };

        task.setOnSucceeded(event -> {
            StyleSpans<Collection<String>> spans = task.getValue();
            if (spans != null) {
                // FIX: 长度检查，防止 IndexOutOfBoundsException
                int currentLength = codeArea.getLength();
                int spansLength = spans.length();

                if (currentLength == spansLength) {
                    codeArea.setStyleSpans(0, spans);
                }
            }
        });

        // 提交任务到线程池
        executor.execute(task);
    }

    @FXML
    private void handleSaveAsLatex() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as LaTeX File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("LaTeX Files", "*.tex"));
        fileChooser.setInitialFileName("output.tex");
        File file = fileChooser.showSaveDialog(editorContainer.getScene().getWindow());
        if (file != null) {
            try {
                viewModel.saveAsLatex(file);
            } catch (IOException | InterruptedException e) {
                showErrorDialog("Save Failed", "An error occurred while saving the LaTeX file.", e.toString());
                e.printStackTrace();
            }
        }
    }

    private void showErrorDialog(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        TextArea textArea = new TextArea(contentText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    @FXML
    private void handleOpen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown Files", "*.md", "*.acadown", "*.txt"));
        File initialDir = new File("sandbox");
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        File file = fileChooser.showOpenDialog(editorContainer.getScene().getWindow());
        if (file != null) {
            try {
                viewModel.loadFile(file);
            } catch (IOException e) {
                showErrorDialog("Open Failed", "Could not load file.", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        File currentFile = viewModel.getCurrentFile();
        if (currentFile != null) {
            try {
                viewModel.saveFile(currentFile);
            } catch (IOException e) {
                showErrorDialog("Save Failed", "Could not save file.", e.getMessage());
            }
        } else {
            handleSaveAs();
        }
    }

    @FXML
    private void handleSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown Files", "*.md", "*.acadown"));
        File initialDir = new File("sandbox");
        if (initialDir.exists()) fileChooser.setInitialDirectory(initialDir);
        File file = fileChooser.showSaveDialog(editorContainer.getScene().getWindow());
        if (file != null) {
            try {
                viewModel.saveFile(file);
            } catch (IOException e) {
                showErrorDialog("Save Failed", "Could not save file.", e.getMessage());
            }
        }
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @FXML
    private void handleExit() {
        shutdown();
        javafx.application.Platform.exit();
    }
}