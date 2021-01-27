package se.alipsa.ride.code.rtab;

import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.Ride;
import se.alipsa.ride.TaskListener;
import se.alipsa.ride.code.CodeComponent;
import se.alipsa.ride.code.CodeTextArea;
import se.alipsa.ride.code.TextAreaTab;
import se.alipsa.ride.console.ConsoleComponent;
import se.alipsa.ride.environment.ContextFunctionsUpdateListener;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RTextArea extends CodeTextArea implements ContextFunctionsUpdateListener {

  // Since T and F are not true keywords (they can be reassigned e.g. T <- FALSE), they are not included below
  private static final String[] KEYWORDS = new String[]{
      "if", "else", "repeat", "while", //"function",
      "for", "in", "next", "break", "TRUE",
      "FALSE", "NULL", "Inf", "NaN", "NA",
      "NA_integer_", "NA_real_", "NA_complex_", "NA_character_", "…"
      //"library" // not strictly a keyword but RStudio treats it like this so we will too
  };

  // See https://www.rdocumentation.org/packages/base/versions/3.5.2 for more, at ns-dblcolon
  // or https://stat.ethz.ch/R-manual/R-devel/library/base/html/00Index.html
  // Will be too long for styling the textarea but useful for suggestions using ctrl + tab
  // see https://github.com/FXMisc/RichTextFX/issues/91 for some ideas
  private static final String[] FUNCTIONS = new String[]{
      "abbreviate", "abs", "acos", "acosh", "activeBindingFunction", "addNA", "addTaskCallback", "aggregate", "agrep",
      "agrepl", "alist", "all", "all.equal", "all.names", "all.vars", "allowInterrupts", "any", "anyDuplicated", "anyMissing",
      "anyNA", "aperm", "append", "apply", "arccos", "arcsin", "arctan", "args", "array", "as.array", "as.call", "as.character",
      "as.complex", "as.data.frame", "as.Date", "as.difftime", "as.double", "as.environment", "as.expression", "as.factor",
      "as.function", "as.hexmode", "as.integer", "as.list", "as.logical", "as.matrix", "as.name", "as.null", "as.numeric",
      "as.ordered", "as.pairlist", "as.POSIXct", "as.POSIXlt", "as.single", "as.symbol", "as.table", "as.vector", "asin",
      "AsIs", "asplit", "asS3", "asS4", "assign", "atan", "atan2", "attach", "attachNamespace", "attr", "attributes",
      "autoload", "autoloader",

      "backsolve", "baseenv", "basename", "beta", "besselI", "besselK", "besselJ", "besselY", "bindtextdomain", "bitwAnd",
      "bitwNot", "bitwOr", "bitwShiftL", "bitwShiftR", "bitwXor", "body", "bquote", "browser", "browserCondition",
      "browserSetDebug", "browserText", "builtins", "by", "bzfile",

      "c", "call", "casefold", "cat", "cbind", "ceiling", "charmatch", "chartr", "chol", "chol2inv", "choose", "class",
      "close", "col", "colMeans", "colnames", "colSums", "comment", "complex", "conflicts", "cos", "cospi", "cummax",
      "cummin", "cumsum", "cumprod", "curlGetHeaders", "cut",

      "data.class", "data.frame", "deparse", "date", "det", "determinant", "detach", "dget", "dbinom", "diag", "diff",
      "difftime", "digamma", "dim", "dimnames", "dnorm", "dir.create", "dir.exists", "dirname", "do.call", "dontCheck",
      "dpois", "dput", "drop", "dunif", "duplicated",

      "emptyenv", "enc2native", "enc2utf8", "Encoding", "encodeString", "env.profile", "environment", "environmentName",
      "exp", "eval", "evalq", "eval.parent", "exists", "expand.grid", "exp", "expression",

      "factor", "factorial", "fifo", "file", "file.access", "file.append", "file.choose", "file.copy", "file.create",
      "file.exists", "file.link", "file.remove", "file.show", "file.symlink", "Filter", "Find", "find.package",
      "findInterval", "floor", "flush", "force", "forceAndCall", "formals", "format", "format.info", "format.pval",
      "formatC", "formatDL", "forwardsolve",

      "gamma", "gc", "gcinfo", "get0", "gettext", "getwd", "gl", "globalenv", "gregexpr", "grep", "grepl", "grepRaw",
      "gsub", "gzcon", "gzfile",

      "iconv", "identical", "identity", "ifelse", "import", "inherits", "integer", "interaction", "interactive",
      "is.array", "is.atomic", "is.call", "is.character", "is.complex", "is.data.frame", "is.double", "is.environment",
      "is.expression", "is.factor", "is.function", "is.integer", "is.language", "is.list", "is.na", "is.null",
      "is.numeric", "is.matrix", "is.object", "is.ordered", "is.primitive", "is.R", "is.recursive", "is.vector",
      "isFALSE", "isTRUE", "isIncomplete", "isOpen", "isSymmetric", "I", "ISOdatetime", "ISOdate",

      "lapply", "lbeta", "lchoose", "lfactorial", "lgamma", "library", "list", "local", "log", "log10",

      "Map", "mapply", "margin.table", "match", "matrix", "max", "mean", "median", "memCompress", "memDecompress", "merge", "min", "mostattributes",

      "names", "Negate", "new", "new.env", "NextMethod", "ngettext", "nrow", "ncol",

      "oldClass", "open", "option", "order", "ordered",

      "parent.env", "pairlist", "paste", "paste0", "path.package", "pbinom", "pipe", "pmax", "pmin", "pnorm", "Position",
      "ppois", "prettyNum", "print", "provideDimnames", "psigamma", "punif",

      "qbinom", "qpois", "qnorm", "quantile", "qunif",

      "range", "rbind", "rbinom", "read.dcf", "require", "regexec", "regexpr", "rep", "return", "round", "rowMeans",
      "rownames", "rowSums", "rpois", "rnorm", "R.home", "Recall", "Reduce", "RNGkind", "RNGversion", "runif",

      "sapply", "scale", "sd", "seq", "set.seed", "setClass", "setRefClass", "setwd", "signif", "sin", "single", "sinpi",
      "socketConnection", "sqrt", "stop", "stopifnot", "str", "strsplit", "sub", "subset", "substr", "sum", "summary",
      "switch", "Sys.chmod", "Sys.Date", "Sys.getenv", "Sys.getpid", "Sys.junction", "Sys.localeconv", "Sys.setFileTime",
      "Sys.sleep", "Sys.time", "Sys.umask", "Sys.which",

      "table", "tan", "tanpi", "toupper", "tolower", "trigamma", "trunc", "tryCatch", "typeof",

      "unclass", "unlist", "unz", "url", "UseMethod",

      "Vectorize",

      "which", "with", "writeLines", "write.dcf",

      "xzfile"
  };

  TreeSet<String> contextFunctions = new TreeSet<>();
  TreeSet<String> contextObjects = new TreeSet<>();

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String FUNCTIONS_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
  private static final String ASSIGNMENT_PATTERN = "->|<-|->>|<<-|=(?!=)|~|%>%|\\$";
  private static final String OPERATOR_PATTERN = "-|\\+|\\*|/|\\^|\\*{2}|%%|%/%|%in%|<|>|<=|>=|={2}|!=|!|&|:";
  private static final String BRACKET_PATTERN = "[\\[\\]\\{\\}\\(\\)]";
  private static final String DIGIT_PATTERN = "\\b\\d+";
  //private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|\'([^\'\\\\]|\\\\.)*\'"; // backtracing makes this crazy slow
  private static final String STRING_PATTERN = "\"\"|''|\"[^\"]+\"|'[^']+'";
  private static final String COMMENT_PATTERN = "#[^\n]*";
  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<FUNCTIONS>" + FUNCTIONS_PATTERN + ")"
          + "|(?<ASSIGNMENT>" + ASSIGNMENT_PATTERN + ")"
          + "|(?<OPERATOR>" + OPERATOR_PATTERN + ")"
          + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
          + "|(?<DIGIT>" + DIGIT_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );
  private static final Pattern LIGHT_PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );
  private static final Logger LOG = LogManager.getLogger(RTextArea.class);
  ContextMenu suggestionsPopup = new ContextMenu();

  public RTextArea() {
  }

  public RTextArea(TextAreaTab parent) {
    super(parent);
    textProperty().addListener((observable, oldValue, newValue) -> {
      if (parent instanceof RTab) {
        if (newValue.contains("hamcrest") || newValue.contains("testthat")) {
          ((RTab) parent).enableRunTestsButton();
        } else {
          ((RTab) parent).disableRunTestsButton();
        }
      }
    });

    Ride gui = parent.getGui();
    ConsoleComponent console = gui.getConsoleComponent();
    addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.isControlDown()) {
        if (KeyCode.ENTER.equals(e.getCode())) {
          CodeComponent codeComponent = gui.getCodeComponent();
          String rCode = getText(getCurrentParagraph()); // current line

          String selected = selectedTextProperty().getValue();
          // if text is selected then go with that instead
          if (selected != null && !"".equals(selected)) {
            rCode = codeComponent.getTextFromActiveTab();
          }
          if (parent instanceof TaskListener) {
            console.runScriptAsync(rCode, codeComponent.getActiveScriptName(), (TaskListener)parent);
          } else {
            console.runScriptAsync(rCode, codeComponent.getActiveScriptName(), new TaskListener() {
              @Override public void taskStarted() { }
              @Override public void taskEnded() { }
            });
          }
          moveTo(getCurrentParagraph() + 1, 0);
          int totalLength = getAllTextContent().length();
          if (getCaretPosition() > totalLength) {
            moveTo(totalLength);
          }
        } else if (KeyCode.SPACE.equals(e.getCode())) {
          autoComplete();
        }
      }
    });

  }

  protected final StyleSpans<Collection<String>> computeHighlighting(String text) {
    return computeFullHighlighting(text);
    /* // rewrote regexp for String pattern so do not need this now
    if (text.length() < 60000) {
      return computeFullHighlighting(text);
    } else {
      log.warn("Text is too large for full syntax coloring, using bare essentials");
      return computeLightHighlighting(text);
    }*/
  }

  protected final StyleSpans<Collection<String>> computeLightHighlighting(String text) {
    Matcher matcher = LIGHT_PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("COMMENT") != null ? "comment" :
                  null; /* never happens */
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  protected final StyleSpans<Collection<String>> computeFullHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("FUNCTIONS") != null ? "function" :
                matcher.group("ASSIGNMENT") != null ? "assign" :
                    matcher.group("OPERATOR") != null ? "operator" :
                        matcher.group("BRACKET") != null ? "bracket" :
                            matcher.group("DIGIT") != null ? "digit" :
                                matcher.group("STRING") != null ? "string" :
                                    matcher.group("COMMENT") != null ? "comment" :
                                        null; /* never happens */
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }

    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  /**
   * TODO: maybe a regex would be more performant?
   * "^.*?(\\w+)\\W*$" is not sufficient as it handles dots as word boundary
   */
  @Override
  public void autoComplete() {
    String line = getText(getCurrentParagraph());
    String currentText = line.substring(0, getCaretColumn());
    //System.out.println("Current text is " + currentText);
    String lastWord;
    int index = currentText.indexOf(' ');
    if (index == -1 ) {
      lastWord = currentText;
    } else {
      lastWord = currentText.substring(currentText.lastIndexOf(' ') + 1);
    }
    index = lastWord.indexOf('(');
    if (index > -1) {
      lastWord = lastWord.substring(index+1);
    }
    index = lastWord.indexOf('[');
    if (index > -1) {
      lastWord = lastWord.substring(index+1);
    }
    index = lastWord.indexOf('{');
    if (index > -1) {
      lastWord = lastWord.substring(index+1);
    }

    //System.out.println("Last word is '" + lastWord + "'");
    if (lastWord.length() > 0) {
      suggestCompletion(lastWord);
    }
  }

  private void suggestCompletion(String lastWord) {
    TreeSet<String> suggestions = new TreeSet<>();
    if (contextFunctions.isEmpty() && contextObjects.isEmpty()) {
      for (String keyword : KEYWORDS) {
        if (keyword.startsWith(lastWord)) {
          suggestions.add(keyword);
        }
      }
      for (String function : FUNCTIONS) {
        if (function.startsWith(lastWord)) {
          suggestions.add(function + "()");
        }
      }
    } else {
      for (String context : contextFunctions) {
        if (context.startsWith(lastWord)) {
          suggestions.add(context + "()");
        }
      }
      for (String obj : contextObjects) {
        if (obj.startsWith(lastWord)) {
          suggestions.add(obj);
        }
      }
    }
    suggestCompletion(lastWord, suggestions, suggestionsPopup);
  }

  @Override
  public void updateContextFunctions(TreeSet<String> functionList, TreeSet<String> objectList) {
    contextFunctions = functionList;
    contextObjects = objectList;
  }
}
