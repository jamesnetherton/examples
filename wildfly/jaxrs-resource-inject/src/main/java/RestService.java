import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class RestService {

    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    private DataSource dataSource;

    @GET
    public String getDatasource() {
        return "ExampleDS = " + dataSource;
    }
}
