package acmecollege;

import acmecollege.entity.NonAcademicStudentClub;
import acmecollege.entity.StudentClub;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.lang.invoke.MethodHandles;
import java.net.URI;

import static acmecollege.utility.MyConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class StudentClubTests {

    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;
    static int record_id = 2;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
                .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
                .scheme(HTTP_SCHEMA)
                .host(HOST)
                .port(PORT)
                .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;

    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
                new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    @Test
    public void test01_getAllStudentClubs_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("studentclub")
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test02_getAllStudentClubs_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("studentclub")
                .request()
                .get();
        assertEquals(response.getStatus(), 403);
    }
    @Test
    public void test03_getStudentClubById_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("studentclub/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test04_getStudentClubById_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("studentclub/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test05_postStudentClub_with_adminrole() throws JsonMappingException, JsonProcessingException {
        StudentClub club = new NonAcademicStudentClub();
        club.setName("Chess Club");
        Response response = webTarget
                .register(adminAuth)
                .path("studentclub")
                .request()
                .post(Entity.json(club));
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test06_postStudentClub_with_userrole() throws JsonMappingException, JsonProcessingException {
        StudentClub club = new NonAcademicStudentClub();
        club.setName("Debate Club");
        Response response = webTarget
                .register(userAuth)
                .path("studentclub")
                .request()
                .post(Entity.json(club));
        assertEquals(response.getStatus(), 403);
    }

    @Test
    public void test07_deleteStudentClub_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("studentclub/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .delete();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test08_deleteStudentClub_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("studentclub/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .delete();
        assertEquals(response.getStatus(), 403);
    }
}