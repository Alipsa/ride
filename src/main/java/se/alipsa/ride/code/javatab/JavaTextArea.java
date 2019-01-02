package se.alipsa.ride.code.javatab;

import javafx.beans.InvalidationListener;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.code.TabTextArea;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaTextArea extends CodeArea implements TabTextArea {

  private File file;

  boolean contentChanged = false;
  private boolean blockChange = false;

  private JavaTab parent;

  private static final String[] KEYWORDS = new String[] {
      "abstract", "assert", "boolean", "break", "byte",
      "case", "catch", "char", "class", "const",
      "continue", "default", "do", "double", "else",
      "enum", "extends", "final", "finally", "float",
      "for", "goto", "if", "implements", "import",
      "instanceof", "int", "interface", "long", "native",
      "new", "package", "private", "protected", "public",
      "return", "short", "static", "strictfp", "super",
      "switch", "synchronized", "this", "throw", "throws",
      "transient", "try", "void", "volatile", "while"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<BRACE>" + BRACE_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );

  public JavaTextArea(JavaTab parent) {
    this.parent = parent;
    setParagraphGraphicFactory(LineNumberFactory.get(this));
    multiPlainChanges()
        .successionEnds(Duration.ofMillis(400))
        .subscribe(ignore -> setStyleSpans(0, computeHighlighting(getText())));

    plainTextChanges().subscribe(ptc -> contentChanged());
  }

  private void contentChanged() {
    if (contentChanged == false && !blockChange) {
      parent.contentChanged();
      contentChanged = true;
    }
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while(matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("PAREN") != null ? "paren" :
                  matcher.group("BRACE") != null ? "brace" :
                      matcher.group("BRACKET") != null ? "bracket" :
                          matcher.group("SEMICOLON") != null ? "semicolon" :
                              matcher.group("STRING") != null ? "string" :
                                  matcher.group("COMMENT") != null ? "comment" :
                                      null; /* never happens */ assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public void setFile(File file) {
    this.file = file;
  }

  @Override
  public String getTextContent() {
    String code;
    String selected = selectedTextProperty().getValue();
    if (selected == null || "".equals(selected)) {
      code = getText();
    } else {
      code = selected;
    }
    return code;
  }

  @Override
  public String getAllTextContent() {
    return getText();
  }

  @Override
  public void replaceContentText(int start, int end, String text) {
    blockChange = true;
    replaceText(start, end, text);
    blockChange = false;
  }
}
