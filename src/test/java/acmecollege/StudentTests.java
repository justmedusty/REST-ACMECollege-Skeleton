package acmecollege;

import acmecollege.entity.Student;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StudentTests {

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
    public void test01_getAllStudents_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("student")
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }
    @Order(value = 1)
    @Test
    public void test02_getAllStudents_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("student")
                .request()
                .get();
        assertEquals(response.getStatus(), 403);
    }
    @Order(2)
    @Test
    public void test03_getStudentById_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("student/1")

                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }
    @Order(3)
    @Test
    public void test04_getStudentById_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("student/1")
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }
    @Order(4)
    @Test
    public void test05_postStudent_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("smith");

        try (Response response = webTarget
                .register(adminAuth)
                .path("student")
                .request()
                .post(Entity.json(student))) {
            assertEquals(response.getStatus(), 200);
        }
    }
    @Order(5)
    @Test
    public void test06_postStudent_with_userrole() throws JsonMappingException, JsonProcessingException {
        Student student = new Student();
        student.setFullName("Jane","Doe");
        try (Response response = webTarget
                .register(userAuth)
                .path("student")
                .request()
                .post(Entity.json(student))) {
            assertEquals(response.getStatus(), 403);
        }
    }
    @Order(6)
    @Test
    public void test07_deleteStudent_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("student/2")
                .request()
                .delete();
        assertEquals(response.getStatus(), 200);
    }

    @Order(7)
    @Test
    public void test08_deleteStudent_with_userrole() throws JsonMappingException, JsonProcessingException {
        try (Response response = webTarget
                .register(userAuth)
                .path("student/{id}")
                .resolveTemplate("id", 1)
                .request()
                .delete()) {
            assertEquals(response.getStatus(), 403);
        }
    }
}