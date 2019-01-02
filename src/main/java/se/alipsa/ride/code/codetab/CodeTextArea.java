package se.alipsa.ride.code.codetab;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.code.TabTextArea;

public class CodeTextArea extends CodeArea implements TabTextArea {

    private File file;

    private static final String[] KEYWORDS = new String[] {
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


    public CodeTextArea() {
        setParagraphGraphicFactory(LineNumberFactory.get(this));
        // recompute the syntax highlighting 400 ms after user stops editing area

        // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
        // multi plain changes = save computation by not rerunning the code multiple times
        //   when making multiple changes (e.g. renaming a method at multiple parts in file)
        multiPlainChanges()

        // do not emit an event until 400 ms have passed since the last emission of previous stream
        .successionEnds(Duration.ofMillis(400))

        // run the following code block when previous stream emits an event
        .subscribe(ignore -> setStyleSpans(0, computeHighlighting(getText())));

    }


    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("ASSIGNMENT") != null ? "assign" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("DIGIT") != null ? "digit" :
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getTextContent() {
        String rCode;
        String selected = selectedTextProperty().getValue();
        if (selected == null || "".equals(selected)) {
            rCode = getText();
        } else {
            rCode = selected;
        }
        return rCode;
    }

    @Override
    public String getAllTextContent() {
        return getText();
    }
}
