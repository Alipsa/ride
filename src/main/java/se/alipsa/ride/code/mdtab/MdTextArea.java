package se.alipsa.ride.code.mdtab;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.TabTextArea;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MdTextArea extends CodeTextArea implements TabTextArea {

  private static String multilinedTagPattern(String tagRegex, String groupName) {
    StringBuilder stringBuilder = new StringBuilder("(\\A|[^\\\\])(?<")
        .append(groupName)
        .append(">")
        .append(tagRegex)
        .append("((?!")
        .append(tagRegex)
        .append(")(.|[\n]))*[^\\\\]")
        .append(tagRegex)
        .append(")");

    return stringBuilder.toString();
  }

  private static String titlePattern(int titleNumber) {
    StringBuilder stringBuilder = new StringBuilder("(?<TITLE")
        .append(titleNumber)
        .append(">((^#{")
        .append(titleNumber)
        .append("})|(\n#{")
        .append(titleNumber)
        .append("}))\\h[^\n]+)");

    return stringBuilder.toString();
  }

  private static final String BOLD_PATTERN = multilinedTagPattern("[*_]{2}", "BOLD");
  private static final String ITALIC_PATTERN = multilinedTagPattern("[*_]", "ITALIC");
  private static final String STRIKETHROUGH_PATTERN = multilinedTagPattern("[~]{2}", "STRIKETHROUGH");
  private static final String TITLE1_PATTERN = titlePattern(1);
  private static final String TITLE2_PATTERN = titlePattern(2);
  private static final String TITLE3_PATTERN = titlePattern(3);
  private static final String TITLE4_PATTERN = titlePattern(4);
  private static final String TITLE5_PATTERN = titlePattern(5);
  private static final String TITLE6_PATTERN = titlePattern(6);
  private static final String TAG_PATTERN = "<.*/?>";
  private static final String BLOCK_CODE_PATTERN = multilinedTagPattern("[`]{3}", "BLOCKCODE");
  private static final String CODE_PATTERN = multilinedTagPattern("[`]", "CODE");

  private static final String ITALICBOLD_PATTERN = multilinedTagPattern("[*_]{3}", "ITALICBOLD");
  private static final String BOLDSTRIKETHROUGH_PATTERN = multilinedTagPattern("([~]{2}[*_]{2})|([*_]{2}[~]{2})", "BOLDSTRIKETHROUGH");
  private static final Pattern PATTERN = Pattern.compile(
      BOLDSTRIKETHROUGH_PATTERN
      + "|" + ITALICBOLD_PATTERN
      + "|" + BOLD_PATTERN
      + "|" + ITALIC_PATTERN
      + "|" + STRIKETHROUGH_PATTERN
      + "|" + TITLE1_PATTERN
      + "|" + TITLE2_PATTERN
      + "|" + TITLE3_PATTERN
      + "|" + TITLE4_PATTERN
      + "|" + TITLE5_PATTERN
      + "|" + TITLE6_PATTERN
      + "|(?<TAG>" + TAG_PATTERN + ")"
      + "|" + BLOCK_CODE_PATTERN
      + "|" + CODE_PATTERN
  );

  public MdTextArea(MdTab parent) {
    plainTextChanges().subscribe(ptc -> {
      if (parent.isChanged() == false && !blockChange) {
        parent.contentChanged();
      }
    });

    addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown() && KeyCode.F.equals(e.getCode())) {
        parent.getGui().getMainMenu().displayFind();
      }
    });
  }

  @Override
  protected StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("TITLE1") != null ? "title1" :
              matcher.group("TITLE2") != null ? "title2" :
                  matcher.group("TITLE3") != null ? "title3" :
                      matcher.group("TITLE4") != null ? "title4" :
                          matcher.group("TITLE5") != null ? "title5" :
                              matcher.group("TITLE6") != null ? "title6" :
                                  matcher.group("BOLDSTRIKETHROUGH") != null ? "boldstrikethrough" :
                                      matcher.group("ITALICBOLD") != null ? "italicbold" :
                                          matcher.group("ITALIC") != null ? "italic" :
                                              matcher.group("BOLD") != null ? "bold" :
                                                  matcher.group("STRIKETHROUGH") != null ? "strikethrough" :
                                                      matcher.group("BLOCKCODE") != null ? "blockcode" :
                                                          matcher.group("CODE") != null ? "code" :
                                                              matcher.group("TAG") != null ? "tag" :
                                                                  null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start(styleClass.toUpperCase()) - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end(styleClass.toUpperCase()) - matcher.start(styleClass.toUpperCase()));
      lastKwEnd = matcher.end(styleClass.toUpperCase());
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }
}
