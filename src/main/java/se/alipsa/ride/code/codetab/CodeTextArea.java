package se.alipsa.ride.code.codetab;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.code.TextCodeArea;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeTextArea extends TextCodeArea {

  private static final String[] KEYWORDS = new String[]{
      "if", "else", "repeat", "while", "function",
      "for", "in", "next", "break", "TRUE",
      "FALSE", "NULL", "Inf", "NaN", "NA",
      "NA_integer_", "NA_real_", "NA_complex_", "NA_character_", "…",
      "library"
  };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String ASSIGNMENT_PATTERN = "\\-\\>|\\<\\-|\\=|\\~|\\%\\>\\%";
  private static final String BRACKET_PATTERN = "\\[|\\]|\\{|\\}|\\(|\\)";
  private static final String DIGIT_PATTERN = "\\b\\d+";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|\'([^\'\\\\]|\\\\.)*\'";
  private static final String COMMENT_PATTERN = "#[^\n]*";

  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<ASSIGNMENT>" + ASSIGNMENT_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<DIGIT>" + DIGIT_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );


  public CodeTextArea(CodeTab parent) {
    super(parent);
    textProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue.contains("hamcrest")) {
        parent.enableRunTestsButton();
      } else {
        parent.disableRunTestsButton();
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
              matcher.group("ASSIGNMENT") != null ? "assign" :
                  matcher.group("BRACKET") != null ? "bracket" :
                      matcher.group("DIGIT") != null ? "digit" :
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
