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

   private static final String MULTI_LINE = "(.|\\R)*?";
   private static final String SINGLE_LINE = "(.)*?";

   private static final String BOLD_PATTERN = "[*_]{2}" + SINGLE_LINE + "[*_]{2}";
   private static final String ITALIC_PATTERN = "[*_]" + SINGLE_LINE + "[*_]";
   private static final String STRIKETHROUGH_PATTERN = "[~]{2}" + SINGLE_LINE + "[~]{2}";
   private static final String TITLE1_PATTERN = titlePattern(1);
   private static final String TITLE2_PATTERN = titlePattern(2);
   private static final String TITLE3_PATTERN = titlePattern(3);
   private static final String TITLE4_PATTERN = titlePattern(4);
   private static final String TITLE5_PATTERN = titlePattern(5);
   private static final String TITLE6_PATTERN = titlePattern(6);
   private static final String LINK_PATTERN = "<.*/?>";
   private static final String BLOCK_CODE_PATTERN = "[`]{3}" + MULTI_LINE + "[`]{3}";
   private static final String CODE_PATTERN = "[`]" + SINGLE_LINE + "[`]";
   private static final String UNDERLINE_PATTERN = "[\\[]" + SINGLE_LINE +"[]]";
   private static final String ITALICBOLD_PATTERN = "[*_]{3}" + SINGLE_LINE + "[*_]{3}";
   private static final String BLOCK_QUOTE_PATTERN = ">[^\n]*";

   private static final Pattern PATTERN = Pattern.compile(
      "(?<ITALICBOLD>" + ITALICBOLD_PATTERN + ")"
         + "|(?<BOLD>" + BOLD_PATTERN + ")"
         + "|(?<ITALIC>" + ITALIC_PATTERN + ")"
         + "|(?<STRIKETHROUGH>" + STRIKETHROUGH_PATTERN + ")"
         + "|" + TITLE1_PATTERN
         + "|" + TITLE2_PATTERN
         + "|" + TITLE3_PATTERN
         + "|" + TITLE4_PATTERN
         + "|" + TITLE5_PATTERN
         + "|" + TITLE6_PATTERN
         + "|(?<LINK>" + LINK_PATTERN + ")"
         + "|(?<BLOCKCODE>" + BLOCK_CODE_PATTERN + ")"
         + "|(?<CODE>" + CODE_PATTERN + ")"
         + "|(?<UNDERLINE>" + UNDERLINE_PATTERN + ")"
         + "|(?<BLOCKQUOTE>" + BLOCK_QUOTE_PATTERN + ")"
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
                              matcher.group("ITALICBOLD") != null ? "italicbold" :
                                 matcher.group("ITALIC") != null ? "italic" :
                                    matcher.group("BOLD") != null ? "bold" :
                                       matcher.group("STRIKETHROUGH") != null ? "strikethrough" :
                                          matcher.group("BLOCKCODE") != null ? "blockcode" :
                                             matcher.group("CODE") != null ? "code" :
                                                matcher.group("UNDERLINE") != null ? "underline" :
                                                   matcher.group("LINK") != null ? "link" :
                                                      matcher.group("BLOCKQUOTE") != null ? "blockquote" :
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
