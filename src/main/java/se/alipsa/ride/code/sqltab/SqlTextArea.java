package se.alipsa.ride.code.sqltab;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import se.alipsa.ride.code.TextCodeArea;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlTextArea extends TextCodeArea {

  private static final String[] KEYWORDS = new String[]{
      "absolute", "action", "add", "admin", "after", "aggregate", "alias", "all", "allocate", "alter",
      "and", "any", "are", "array", "as", "asc", "assertion", "assertion", "at", "atomic",

      "authorization", "before", "begin", "bigint", "binary", "bit", "blob", "boolean",
      "both", "breadth", "by", "call",

      "cascade", "cascaded", "case", "cast", "catalog", "char", "character", "check",
      "class", "clob", "close", "collate", "collation", "collect", "column", "commit",
      "completion", "condition", "connect", "connection", "constraint", "constraints", "constructor", "contains",
      "continue", "corresponding", "create", "cross", "cube", "current", "current_date", "current_path",
      "current_role", "current_time", "current_timestamp", "current_user", "cursor", "cycle", "data", "datalink",

      "date", "day", "deallocate", "dec", "decimal", "declare", "default", "deferrable",
      "delete", "depth", "deref", "desc", "descriptor", "destructor", "diagnostics", "dictionary",
      "disconnect", "distinct", "do", "domain", "double", "drop",

      "element", "end", "end-exec", "equals", "escape", "except", "exception", "execute",
      "exists", "exit", "expand", "expanding",

      "false", "first", "float", "for", "foreign", "free", "from", "function", "fusion",

      "general", "get", "global", "goto", "group", "grouping",

      "handler", "hash", "hour",

      "identity", "if", "ignore", "immediate", "in", "indicator", "initialize", "initially", "inner",
      "inout", "input", "insert", "int", "integer", "intersect", "intersection", "interval", "into",
      "is", "isolation", "iterate", "join", "key",

      "language", "large", "last", "lateral", "leading", "leave", "left", "less",
      "level", "like", "limit", "local", "localtime", "localtimestamp", "locator", "loop",

      "match", "member", "meets", "merge", "minute", "modifies", "modify", "module", "month", "multiset",

      "names", "national", "natural", "nchar", "nclob", "new", "next", "no", "none", "normalize",
      "not", "null", "numeric",

      "object", "of", "off", "old", "on", "only", "open", "operation", "option",
      "or", "order", "ordinality", "out", "outer", "output",

      "pad", "parameter", "parameters", "partial", "path", "period",
      "postfix", "precedes", "precision", "prefix", "preorder", "prepare", "preserve", "primary",
      "prior", "privileges", "procedure", "public",

      "read", "reads", "real", "recursive", "redo", "ref", "references", "referencing",
      "relative", "repeat", "resignal", "restrict", "result", "return", "returns", "revoke",
      "right", "role", "rollback", "rollup", "routine", "row", "rows",

      "savepoint", "schema", "scroll", "search", "second", "section", "select", "sequence", "session",
      "session_user", "set", "sets", "signal", "size", "smallint", "specific", "specifictype",
      "sql", "sqlexception", "sqlstate", "sqlwarning", "start", "state", "static", "structure",
      "submultiset", "succeeds", "sum", "system_user",

      "table", "tablesample", "temporary", "terminate", "than", "then", "time", "timestamp",
      "timezone_hour", "timezone_minute", "to", "trailing", "transaction", "translation", "treat",
      "trigger", "true", "uescape",

      "under", "undo", "union", "unique", "unknown", "until", "update", "usage", "user", "using",

      "value", "values", "varchar", "variable", "varying", "view",

      "when", "whenever", "where", "while", "with", "write",

      "year",

      "zone"
  };

  private static final String KEYWORD_PATTERN = "(?i)\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String SEMICOLON_PATTERN = "\\;";
  //private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"" + "|" + "\'([^\'\\\\]|\\\\.)*\'";
  private static final String STRING_PATTERN = "\"\"|''|\"[^\"]+\"|'[^']+'";
  private static final String COMMENT_PATTERN = "--[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

  private static final Pattern PATTERN = Pattern.compile(
      "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
          + "|(?<PAREN>" + PAREN_PATTERN + ")"
          + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
          + "|(?<STRING>" + STRING_PATTERN + ")"
          + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
  );

  public SqlTextArea(SqlTab parent) {
    super(parent);
  }

  @Override
  protected StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder
        = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null ? "keyword" :
              matcher.group("PAREN") != null ? "paren" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
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
