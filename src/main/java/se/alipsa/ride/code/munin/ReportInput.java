package se.alipsa.ride.code.munin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ReportInput {

  private String params;
  private Stage stage;

  public static final TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {};

  public ReportInput(Stage stage) {
    this.stage = stage;
  }

  public void addParams(String jsonParams) {
    //System.out.println("ReportInput: Got " + jsonParams);
    this.params = jsonParams;
    stage.close();
  }

  Map<String, Object> asMap() throws JsonProcessingException {
    if (params == null) return null;
    return new ObjectMapper().readValue(params, TYPE_REF);
  }
}
