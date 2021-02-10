package se.alipsa.ride.code.munin;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import se.alipsa.ride.model.MuninConnection;
import se.alipsa.ride.model.MuninReport;

import java.util.List;
import java.util.stream.Collectors;

public class MuninClient {


  private static Client createClient(MuninConnection con) {
    Client client = ClientBuilder.newClient();
    HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(con.getUserName(), con.getPassword());
    client.register(feature);
    return client;
  }

  public static List<String> fetchReportGroups(MuninConnection con) throws Exception {
    Client client = createClient(con);
    WebTarget target = client.target(con.target()).path("/api/getReportGroups");
    Response response;
    try {
      response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
    } catch (ProcessingException e) {
      throw new Exception("Failed to fetch report groups on Munin server: " + con.target(), e);
    }
    if (response.getStatus() != 200) {
      throw new Exception("Failed to fetch report groups on Munin server: " + con.target()
                          + ". The response code was " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase());
    }
    return response.readEntity(new GenericType<List<String>>() {});
  }

  public static List<MuninReport> fetchReports(MuninConnection con, String groupName) throws Exception {
    Client client = createClient(con);
    WebTarget target = client.target(con.target()).path("/api/getReports").queryParam("groupName", groupName);
    Response response;
    try {
      response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
    } catch (ProcessingException e) {
      throw new Exception("Failed to fetch reports on Munin server: " + con.target(), e);
    }
    if (response.getStatus() != 200) {
      throw new Exception("Failed to fetch reports on Munin server: " + con.target()
                          + ". The response code was " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase()
      );
    }
    return response.readEntity(new GenericType<List<MuninReport>>() {});
  }

  public static void publishReport(MuninConnection muninConnection, MuninReport muninReport, boolean newReport) throws Exception {
    if(newReport) {
      addReport(muninConnection, muninReport);
    } else {
      updateReport(muninConnection, muninReport);
    }
  }

  public static void addReport(MuninConnection con, MuninReport report) throws Exception {
    upsertReport(con, report, "/api/addReport", HttpMethod.POST);
  }

  public static void updateReport(MuninConnection con, MuninReport report) throws Exception {
    upsertReport(con, report, "/api/updateReport", HttpMethod.PUT);
  }

  private static void upsertReport(MuninConnection con, MuninReport report, String url, String method) throws Exception {
    Client client = createClient(con);
    WebTarget target = client.target(con.target());
    Response response;
    try {
      if (HttpMethod.PUT.equals(method)) {
        response = target.path(url).request()
            .put(Entity.entity(report, MediaType.APPLICATION_JSON_TYPE));
      } else if (HttpMethod.POST.equals(method)) {
        response = target.path(url).request()
            .post(Entity.entity(report, MediaType.APPLICATION_JSON_TYPE));
      } else {
        throw new IllegalArgumentException("Unknown method: "  + method);
      }
    } catch (ProcessingException e) {
      throw new Exception("Failed to update report on Munin server: " + con.target(), e);
    }
    if (response.getStatus() != 200) {
      String headers = response.getHeaders().entrySet().stream()
          .map(e -> e.getKey() + "=" + e.getValue())
          .collect(Collectors.joining(", ", "{", "}"));
      throw new Exception("Failed to publish report to Munin server: " + con.target()
                          + ". The response code was " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase()
                          + "\n, Error message was '" + response.readEntity(String.class) + "'"
      );
    }
  }


}
