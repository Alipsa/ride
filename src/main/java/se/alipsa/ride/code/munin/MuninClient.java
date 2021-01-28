package se.alipsa.ride.code.munin;

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

  public static void updateReport(MuninConnection con, MuninReport report) throws Exception {
    Client client = createClient(con);
    WebTarget target = client.target(con.target());
    Response response;
    try {
      response = target.path("/api/updateReport").request()
          .put(Entity.entity(report, MediaType.APPLICATION_JSON_TYPE));
    } catch (ProcessingException e) {
      throw new Exception("Failed to update report on Munin server: " + con.target(), e);
    }
    if (response.getStatus() != 200) {
      throw new Exception("Failed to update report on Munin server: " + con.target()
                          + ". The response code was " + response.getStatus() + " " + response.getStatusInfo().getReasonPhrase()
      );
    }
  }
}
