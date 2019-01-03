package se.alipsa.ride.code;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;

import java.io.File;
import java.time.Duration;
import java.util.Collection;

public abstract class TextCodeArea extends CodeArea implements TabTextArea {
  protected File file;

  protected boolean blockChange = false;

  public TextCodeArea() {
  }

  public TextCodeArea(TextAreaTab parent) {
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

    plainTextChanges().subscribe(ptc -> {
      if (parent.isChanged() == false && !blockChange) {
        parent.contentChanged();
      }
    });
  }

  protected abstract StyleSpans<Collection<String>> computeHighlighting(String text);

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
