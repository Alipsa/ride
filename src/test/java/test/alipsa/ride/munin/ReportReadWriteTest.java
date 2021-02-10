package test.alipsa.ride.munin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.alipsa.ride.code.munin.ReportType.UNMANAGED;

import org.junit.jupiter.api.Test;
import se.alipsa.ride.model.MuninReport;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ReportReadWriteTest {

  @Test
  public void testWriteAndReadReport() throws JAXBException, IOException, XMLStreamException {
    MuninReport report = new MuninReport();
    report.setReportName("Hello Example");
    report.setDescription("Hello World");
    report.setDefinition("\"library(\"se.alipsa:htmlcreator\")\n" +
                         "html.clear()\n" +
                         "html.add(\"<h1>Hello World</h1>\")\n" +
                         "html.content()\"");
    report.setInputContent("");
    report.setReportType(UNMANAGED);
    report.setReportGroup("Examples");

    JAXBContext context = JAXBContext.newInstance(MuninReport.class);
    Marshaller mar= context.createMarshaller();
    mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    File file = File.createTempFile("MuninReport", MuninReport.FILE_EXTENSION);
    mar.marshal(report, file);
    System.out.println("Wrote file " + file.getAbsolutePath());

    MuninReport r2;
    XMLInputFactory xmlInFact = XMLInputFactory.newInstance();
    try(Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      XMLStreamReader xmlReader = xmlInFact.createXMLStreamReader(reader);
      r2 = context.createUnmarshaller().unmarshal(xmlReader, MuninReport.class).getValue();
    }
    assertEquals(report, r2);

  }
}
