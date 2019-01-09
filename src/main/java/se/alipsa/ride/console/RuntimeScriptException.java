package se.alipsa.ride.console;

public class RuntimeScriptException extends Exception {

  public RuntimeScriptException() {
    super();
  }

  public RuntimeScriptException(String s) {
    super(s);
  }

  public RuntimeScriptException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public RuntimeScriptException(Throwable throwable) {
    super(throwable);
  }

  protected RuntimeScriptException(String s, Throwable throwable, boolean b, boolean b1) {
    super(s, throwable, b, b1);
  }
}
