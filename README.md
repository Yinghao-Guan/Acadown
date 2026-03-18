# Acadown

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![JavaFX](https://img.shields.io/badge/GUI-JavaFX-orange)
![ANTLR4](https://img.shields.io/badge/Parser-ANTLR4-red)
![License](https://img.shields.io/badge/License-MIT-green)

**Acadown** is a custom markup language designed to provide modern syntax sugar for LaTeX.

Whilst it borrows the familiar syntactic design of Markdown to ensure a gentle learning curve, Acadown is fundamentally distinct. It is engineered specifically for academic writing and professional typesetting, bridging the gap between the simplicity of plain text and the rigorous standards of LaTeX.

This repository contains both the reference compiler for the Acadown language and a dedicated IDE environment.

![Demo](docs/images/Demo.gif)

## ✨ Philosophy & Features

Acadown is not merely another Markdown dialect; it is a domain-specific language (DSL) for document compilation.

* **Modern Syntax for LaTeX**:
  * Acadown abstracts away the verbosity of raw LaTeX without sacrificing its power.
  * The core compiler translates high-level markup into professional-grade typesetting instructions.
  * **Robust Parsing**: Unlike regex-based Markdown parsers, the Acadown language is defined by a formal grammar using **ANTLR4**, ensuring deterministic and accurate parsing of complex nested structures.

* **Academic-First Design**:
  * **Mathematical Typesetting**: Native, first-class support for LaTeX-style mathematical formulae (`MathNode`) within the markup.
  * **Citation Management**: Integrated CSL (Citation Style Language) generation (`CslGenerator`) and metadata processing, allowing seamless management of bibliographies and academic references directly from the markup source.

* **The Acadown Workbench (`acadown-ide`)**:
  * A reference IDE implementation designed to showcase the language's capabilities.
  * **WYSIWYG PDF Preview**: Instead of converting to HTML, the IDE integrates **Mozilla PDF.js** to provide a real-time, high-fidelity preview of the final rendered document.
  * **Live Compilation**: Instant feedback loop between your Acadown source code and the compiled output.

## 🏗 System Architecture

The project is organised as a Maven multi-module system, separating the language definition from the tooling:

* **`acadown-core` (The Compiler)**:
  * The heart of the project. Contains the formal `.g4` grammar definitions (ANTLR) for the Acadown language.
  * Responsible for lexical analysis, parsing, and constructing the Abstract Syntax Tree (AST).
  * Handles semantic analysis for academic features like citations and cross-references.

* **`acadown-ide` (The Environment)**:
  * A JavaFX-based graphical interface for writing Acadown documents.
  * Integrates the core compiler with a WebView-based PDF renderer.
  * Provides syntax highlighting (`SyntaxHighlighter`) and file management tailored to the Acadown workflow.

## 🚀 Getting Started

### Prerequisites
* JDK 17 or higher
* Maven 3.x

### Build the Compiler & IDE

Clone the repository and build the modules:

```bash
git clone [https://github.com/yinghao-guan/acadown.git](https://github.com/yinghao-guan/acadown.git)
cd acadown
mvn clean install
```

### Launch the Environment

To start the reference IDE:

```bash
mvn javafx:run -pl acadown-ide
```

Alternatively, you may execute the `com.guaguaaaa.acadown.ide.MainApp` class directly from your preferred IDE.

## 🛠 Development Guide

This project welcomes contributions aimed at refining the Acadown language specification or improving the compiler.

1.  **Language Specification**: The formal grammar works are located in `acadown-core/src/main/antlr4/.../AcadownParser.g4`. Modifications here define the syntax of the language itself.
2.  **Compiler Logic**: The transformation from text to AST is handled within the `com.guaguaaaa.acadown.core.ast` package.
3.  **Citation Logic**: Research regarding bibliography parsing and CSL generation can be found in `com.guaguaaaa.acadown.core.util`.

## 📝 Roadmap

* [ ] Expand the formal grammar to support advanced LaTeX environments (tables, figures).
* [ ] Implement configurable compiler backends (e.g., direct-to-PDF vs intermediate TeX).
* [ ] Enhance the Citation Style Language (CSL) engine for broader compatibility.
* [ ] Optimise the compiler's AST traversal for large manuscripts.

## 🤝 Contributing

Contributions are welcome. If you wish to propose changes to the language syntax itself, please open an Issue for discussion to ensure backward compatibility and design consistency.

## 📄 License

[MIT License](LICENSE)