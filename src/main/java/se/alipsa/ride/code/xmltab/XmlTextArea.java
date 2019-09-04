package se.alipsa.ride.code.xmltab;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.code.CodeTextArea;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlTextArea extends CodeTextArea {

  private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENT>(</?\\h*)(\\w+|\\.)+([^<>]*)(\\h*/?>))"
      + "|(?<COMMENT>(?s)<!--.*?-->)");

  private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

  private static final int GROUP_OPEN_BRACKET = 2;
  private static final int GROUP_ELEMENT_NAME = 3;
  private static final int GROUP_ATTRIBUTES_SECTION = 4;
  private static final int GROUP_CLOSE_BRACKET = 5;
  private static final int GROUP_ATTRIBUTE_NAME = 1;
  private static final int GROUP_EQUAL_SYMBOL = 2;
  private static final int GROUP_ATTRIBUTE_VALUE = 3;

  public XmlTextArea(XmlTab parent) {
    super.setParentTab(parent);
    setParagraphGraphicFactory(LineNumberFactory.get(this));

    textProperty().addListener((obs, oldText, newText) -> {
      setStyleSpans(0, computeHighlighting(newText));
    });

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

  protected final StyleSpans<Collection<String>> computeHighlighting(String text) {

    Matcher matcher = XML_TAG.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {

      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      if (matcher.group("COMMENT") != null) {
        spansBuilder.add(Collections.singleton("xml_comment"), matcher.end() - matcher.start());
      } else {
        if (matcher.group("ELEMENT") != null) {
          String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);

          spansBuilder.add(Collections.singleton("xml_tagmark"), matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET));
          spansBuilder.add(Collections.singleton("xml_anytag"), matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));

          if (!attributesText.isEmpty()) {

            lastKwEnd = 0;

            Matcher amatcher = ATTRIBUTES.matcher(attributesText);
            while (amatcher.find()) {
              spansBuilder.add(Collections.emptyList(), amatcher.start() - lastKwEnd);
              spansBuilder.add(Collections.singleton("xml_attribute"), amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
              spansBuilder.add(Collections.singleton("xml_tagmark"), amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
              spansBuilder.add(Collections.singleton("xml_avalue"), amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
              lastKwEnd = amatcher.end();
            }
            if (attributesText.length() > lastKwEnd)
              spansBuilder.add(Collections.emptyList(), attributesText.length() - lastKwEnd);
          }

          lastKwEnd = matcher.end(GROUP_ATTRIBUTES_SECTION);

          spansBuilder.add(Collections.singleton("xml_tagmark"), matcher.end(GROUP_CLOSE_BRACKET) - lastKwEnd);
        }
      }
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }
}
