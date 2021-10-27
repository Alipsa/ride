package se.alipsa.ride.code.jstab;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.code.CodeTextArea;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsTextArea extends CodeTextArea {

  private static final String[] KEYWORDS = new String[]{
      "await",	"break",	"case",	"catch",	"class",
      "const",	"continue",	"debugger",	"default",	"delete",
      "do",	"else",	"enum",	"export",	"extends",
      "false",	"finally", "for",	"function",
      "if", "implements",	"import",	"in",	"instanceof",	"interface",
      "let",	"new",	"null",	"package",	"private",
      "protected", "public",	"return",	"super",	"switch",
      "static",	"this",	"throw", "try", "true",
      "typeof",	"var",	"void",	"while",	"with",
      "yield"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN = "\"\"|''|\"[^\"]+\"|'[^']+'";
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

  public JsTextArea(JsTab parent) {
    super(parent);
    addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown()) {
        if (KeyCode.ENTER.equals(e.getCode())) {
          CodeComponent codeComponent = parent.getGui().getCodeComponent();
          String jsCode = getText(getCurrentParagraph()); // current line

          String selected = selectedTextProperty().getValue();
          // if text is selected then go with that instead
          if (selected != null && !"".equals(selected)) {
            jsCode = codeComponent.getTextFromActiveTab();
          }
          parent.runJavascript(jsCode);
          moveTo(getCurrentParagraph() + 1, 0);
          int totalLength = getAllTextContent().length();
          if (getCaretPosition() > totalLength) {
            moveTo(totalLength);
          }
        }
      }
    });
  }

  protected final StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("PAREN") != null ? "paren" :
                  matcher.group("BRACE") != null ? "brace" :
                      matcher.group("BRACKET") != null ? "bracket" :
                          matcher.group("SEMICOLON") != null ? "semicolon" :
                              matcher.group("STRING") != null ? "string" :
                                  matcher.group("COMMENT") != null ? "comment" :
                                      null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

}
