package se.alipsa.ride.code.codetab;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.alipsa.ride.code.TextCodeArea;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeTextArea extends TextCodeArea {

  private static Logger log = LoggerFactory.getLogger(CodeTextArea.class);

  // Since T and F are not true keywords (they can be reassigned e.g. T <- FALSE), they are not included below
  private static final String[] KEYWORDS = new String[]{
      "if", "else", "repeat", "while", "function",
      "for", "in", "next", "break", "TRUE",
      "FALSE", "NULL", "Inf", "NaN", "NA",
      "NA_integer_", "NA_real_", "NA_complex_", "NA_character_", "…",
      "library" // not strictly a keyword but RStudio treats it like this so we will too
  };

  // See https://www.rdocumentation.org/packages/base/versions/3.5.2 for more, at ns-dblcolon
  // or https://stat.ethz.ch/R-manual/R-devel/library/base/html/00Index.html
  // Will be too long for styling the textarea but useful for suggestions using ctrl + tab
  // see https://github.com/FXMisc/RichTextFX/issues/91 for some ideas
  private static final String[] FUNCTIONS = new String[] {
      "abs", "acos", "addNA", "aggregate", "agrep", "alist", "all", "all.equal", "all.names", "all.vars", "any", "anyDuplicated", "anyNA", "apply", "append", "aperm", "array", "args", "asin", "atan", "atan2", "as.array", "as.call", "as.character", "as.complex", "as.data.frame", "as.Date", "as.difftime", "as.double", "as.environment", "as.expression", "as.factor", "as.hexmode", "as.integer", "as.list", "as.null", "as.matrix", "as.numeric", "as.ordered", "as.pairlist", "as.POSIXct", "as.POSIXlt", "as.single", "as.vector", "attach", "attr", "attributes",
      "backsolve", "baseenv", "basename", "beta", "besselI", "besselK", "besselJ", "besselY", "bindtextdomain", "bitwAnd", "bitwNot", "bitwOr", "bitwShiftL", "bitwShiftR", "bitwXor", "body", "bquote", "browser", "browserCondition", "browserSetDebug", "browserText", "builtins", "by", "bzfile",
      "c", "call", "casefold", "cat", "cbind", "ceiling", "charmatch", "chartr", "chol", "chol2inv", "choose", "class", "close", "col", "colMeans", "colnames", "colSums", "comment", "complex", "conflicts", "cos", "cospi", "cummax", "cummin", "cumsum", "cumprod", "curlGetHeaders", "cut",
      "data.class", "data.frame", "deparse", "date", "det", "determinant", "detach", "dget", "dbinom", "diag", "diff", "difftime", "digamma", "dim", "dimnames", "dnorm", "dir.create", "dir.exists", "dirname", "do.call", "dontCheck", "dpois", "dput", "drop", "dunif", "duplicated",
      "emptyenv", "enc2native", "enc2utf8", "Encoding", "encodeString", "env.profile", "environment", "environmentName", "exp", "eval", "evalq", "eval.parent", "exists", "expand.grid", "expression",
      "factor", "factorial", "fifo", "file", "file.access", "file.append", "file.choose", "file.copy", "file.create", "file.exists", "file.link", "file.remove", "file.show", "file.symlink", "Filter", "Find", "find.package", "findInterval", "floor", "flush", "force", "forceAndCall", "formals", "format", "format.info", "format.pval", "formatC", "formatDL", "forwardsolve",
      "gamma", "gc", "gcinfo", "get0", "gettext", "getwd", "gl", "globalenv", "gregexpr", "grep", "grepl", "grepRaw", "gsub", "gzcon", "gzfile",
      "iconv", "identical", "identity", "ifelse", "inherits", "integer", "interaction", "interactive", "is.array", "is.atomic", "is.call", "is.character", "is.complex", "is.data.frame", "is.double", "is.environment", "is.expression", "is.factor", "is.function", "is.integer", "is.language", "is.na", "is.null", "is.numeric", "is.matrix", "is.object", "is.ordered", "is.primitive", "is.R", "is.recursive", "is.vector", "isFALSE", "isTRUE", "isIncomplete", "isOpen", "isSymmetric", "I", "ISOdatetime", "ISOdate",
      "lapply", "lbeta", "lchoose", "lfactorial", "lgamma", "library", "list", "local", "log", "log10",
      "Map", "mapply", "margin.table", "match", "matrix", "max", "mean",  "median", "memCompress", "memDecompress", "merge", "min", "mostattributes",
      "names", "Negate", "new.env", "NextMethod", "ngettext", "nrow", "ncol",
      "ordered", "oldClass", "open",
      "parent.env", "pairlist", "paste", "paste0", "path.package", "pbinom", "pipe", "pmax", "pmin", "pnorm", "Position", "ppois", "prettyNum", "print", "provideDimnames", "psigamma",
      "qbinom", "qpois", "qnorm", "quantile",
      "range", "rbind", "rbinom", "read.dcf", "require", "regexec", "regexpr", "rep", "return", "round", "rowMeans", "rownames", "rowSums", "rpois", "rnorm", "R.home", "Recall", "Reduce", "RNGkind", "RNGversion",
      "sapply", "scale", "sd", "seq", "set.seed", "setwd", "signif", "sin", "single", "sinpi", "socketConnection", "sqrt",  "stop", "stopifnot", "str", "strsplit", "sub", "subset", "substr", "sum", "summary", "switch", "Sys.chmod", "Sys.Date", "Sys.getenv", "Sys.getpid", "Sys.junction", "Sys.localeconv", "Sys.setFileTime", "Sys.sleep", "Sys.time", "Sys.umask", "Sys.which",
      "table", "tan", "tanpi", "toupper", "tolower", "trigamma", "trunc", "tryCatch", "typeof",
      "unclass", "unz", "url", "UseMethod",
      "Vectorize",
      "which",  "with", "writeLines", "write.dcf",
      "xzfile"
  };


  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  //private static final String FUNCTIONS_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
  private static final String ASSIGNMENT_PATTERN = "->|<-|=(?!=)|~|%>%";
  private static final String OPERATOR_PATTERN = "-|\\+|\\*|/|\\^|\\*{2}|%%|%/%|%in%|<|>|<=|>=|={2}|!=|!|&|:";
  private static final String BRACKET_PATTERN = "[\\[\\]\\{\\}\\(\\)]";
  private static final String DIGIT_PATTERN = "\\b\\d+";
  //private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|\'([^\'\\\\]|\\\\.)*\'"; // backtracing makes this crazy slow
  private static final String STRING_PATTERN = "\"\"|''|\"[^\"]+\"|'[^']+'";
  private static final String COMMENT_PATTERN = "#[^\n]*";

  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          //+ "|(?<FUNCTIONS>" + FUNCTIONS_PATTERN + ")"
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
              // matcher.group("FUNCTIONS") != null ? "function" :
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
}
