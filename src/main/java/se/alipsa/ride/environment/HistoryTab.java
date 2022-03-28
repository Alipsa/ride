package se.alipsa.ride.environment;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import se.alipsa.ride.Constants;
import se.alipsa.ride.UnStyledCodeArea;

import java.time.LocalDateTime;

public class HistoryTab extends Tab {

  private final UnStyledCodeArea historyTa;

  public HistoryTab() {
    setText("History");
    BorderPane borderPane = new BorderPane();
    historyTa = new UnStyledCodeArea();
    historyTa.setEditable(false);
    historyTa.getStyleClass().add("history");
    VirtualizedScrollPane<StyleClassedTextArea> historyScrollPane = new VirtualizedScrollPane<>(historyTa);

    FlowPane buttonPane = new FlowPane();
    buttonPane.setPadding(new Insets(1, 10, 1, 5));
    buttonPane.setHgap(Constants.HGAP);
    Button clearButton = new Button("Clear");
    clearButton.setOnAction(e -> historyTa.clear());
    buttonPane.getChildren().add(clearButton);
    borderPane.setTop(buttonPane);
    borderPane.setCenter(historyScrollPane);
    setContent(borderPane);
  }

  public void addInputHistory(String text) {
    historyTa.appendText("\n<<< input (" + LocalDateTime.now() + ")\n" + text + "\n<<<\n");
  }

  public void addOutputHistory(String text) {
    historyTa.appendText( ">>> output (" + LocalDateTime.now() + ")\n" + text + ">>>\n");
    scrollToEnd();
  }

  private void scrollToEnd() {
    historyTa.moveTo(historyTa.getLength());
    historyTa.requestFollowCaret();
  }

  public void clearHistory() {
    historyTa.clear();
  }

  public void rRestarted() {
    historyTa.appendText("\n\n*********************\n* Session Restarted *\n*********************\n");
    scrollToEnd();
  }
}
