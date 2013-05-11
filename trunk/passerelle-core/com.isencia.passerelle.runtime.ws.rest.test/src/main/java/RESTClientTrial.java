import javax.ws.rs.core.MediaType;
import com.isencia.passerelle.runtime.ws.rest.CodeList;
import com.isencia.passerelle.runtime.ws.rest.FlowHandleResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.DevNullActor;

public class RESTClientTrial {

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
//    ClientConfig config = new DefaultClientConfig();
//    config.getClasses().add(JSONRootElementProvider.class);
    Client client = Client.create();
    client.addFilter(new LoggingFilter());
    WebResource webResource = client.resource("http://localhost/rest/flows");
    
    CodeList codeList = webResource.path("allCodes").accept(MediaType.APPLICATION_JSON).get(CodeList.class);
    System.out.println("all codes "+codeList.getCodes());

    FlowHandleResource handleResource = webResource.path("activeFlow").queryParam("code", "TTT").accept(MediaType.APPLICATION_JSON).get(FlowHandleResource.class);
    System.out.println("active flow "+handleResource);
    
    Flow f = buildTrivialFlow("HiThere");
    FlowHandleResource handleResource2 = webResource.queryParam("code", "MYCODE").type(MediaType.APPLICATION_XML).post(FlowHandleResource.class, f.exportMoML());
    
    System.out.println("committed flow "+handleResource2.getResourceLocation());
  }

  public static Flow buildTrivialFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, sink);

    return flow;
  }

}
