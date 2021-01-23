package se.alipsa.ride.code.mdrtab;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.renjin.sexp.SEXP;
import se.alipsa.ride.Ride;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.CodeType;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.inout.InoutComponent;
import se.alipsa.ride.utils.ExceptionAlert;

import java.io.File;
import java.util.Collections;

public class MdrTab extends TextAreaTab {

  private final Button viewButton;
  private MdrTextArea mdrTextArea;

  public MdrTab(String title, Ride gui) {
    super(gui, CodeType.MDR);
    setTitle(title);
    viewButton = new Button();
    viewButton.setGraphic(new ImageView(IMG_VIEW));
    viewButton.setTooltip(new Tooltip("Render and view"));
    viewButton.setOnAction(this::viewMdr);
    buttonPane.getChildren().add(viewButton);
    mdrTextArea = new MdrTextArea(this);
    VirtualizedScrollPane<MdrTextArea> txtPane = new VirtualizedScrollPane<>(mdrTextArea);
    pane.setCenter(txtPane);
  }

  private void viewMdr(ActionEvent actionEvent) {
    ConsoleComponent consoleComponent = gui.getConsoleComponent();
    consoleComponent.running();
    String viewerTabName = getTitle();
    InoutComponent inout = gui.getInoutComponent();

    Task<Void> task = new Task<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          SEXP htmlContent = consoleComponent.runScriptSilent("library('se.alipsa:mdr2html')\n renderMdr(mdrContent)", Collections.singletonMap("mdrContent", getTextContent()));
          inout.viewHtmlWithBootstrap(htmlContent, viewerTabName);
        } catch (RuntimeException e) {
          throw new Exception(e);
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      consoleComponent.waiting();
    });

    task.setOnFailed(e -> {
      Throwable throwable = task.getException();
      Throwable ex = throwable.getCause();
      if (ex == null) {
        ex = throwable;
      }
      consoleComponent.waiting();
      ExceptionAlert.showAlert(ex.getMessage(), ex);
      gui.getConsoleComponent().promptAndScrollToEnd();
    });
    Thread thread = new Thread(task);
    thread.setDaemon(false);
    consoleComponent.startThreadWhenOthersAreFinished(thread, "mdrFile");
  }

  @Override
  public CodeTextArea getCodeArea() {
    return mdrTextArea;
  }

  @Override
  public File getFile() {
    return mdrTextArea.getFile();
  }

  @Override
  public void setFile(File file) {
    mdrTextArea.setFile(file);
  }

  @Override
  public String getTextContent() {
    return mdrTextArea.getTextContent();
  }

  @Override
  public String getAllTextContent() {
    return mdrTextArea.getAllTextContent();
  }

  @Override
  public void replaceContentText(int start, int end, String content) {
    mdrTextArea.replaceContentText(start, end, content);
  }
}
