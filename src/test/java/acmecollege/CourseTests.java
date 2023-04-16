package acmecollege;

import acmecollege.entity.Course;
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
public class CourseTests {

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
    public void test01_getAllCourses_with_adminrole() throws JsonMappingException{
        Response response = webTarget
                .register(adminAuth)
                .path("course")
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test02_getAllCourses_with_userrole() throws JsonMappingException {
        Response response = webTarget
                .register(userAuth)
                .path("course")
                .request()
                .get();
        assertEquals(response.getStatus(), 403);
    }

    @Test
    public void test03_getCourseById_with_adminrole() throws JsonMappingException {
        Response response = webTarget
                .register(adminAuth)
                .path("course/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test04_getCourseById_with_userrole() throws JsonMappingException{
        Response response = webTarget
                .register(userAuth)
                .path("course/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test05_postCourse_with_adminrole() throws JsonMappingException{
        Course course = new Course();
        course.setCourseTitle("Computer Science 101");
        course.setId(2);
        try (Response response = webTarget
                .register(adminAuth)
                .path("course")
                .request()
                .post(Entity.json(course))) {
            assertEquals(response.getStatus(), 200);
        }
    }

    @Test
    public void test06_postCourse_with_userrole() throws JsonMappingException{
        Course course = new Course();
        course.setCourseTitle("Computer Science 102");
        try (Response response = webTarget
                .register(userAuth)
                .path("course")
                .request()
                .post(Entity.json(course))) {
            assertEquals(response.getStatus(), 403);
        }
    }

    @Test
    public void test07_deleteCourse_with_adminrole() throws JsonMappingException{
        try (Response response = webTarget
                .register(adminAuth)
                .path("course/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .delete()) {
            assertEquals(response.getStatus(), 200);
        }
    }

    @Test
    public void test08_deleteCourse_with_userrole() throws JsonMappingException{
        try (Response response = webTarget
                .register(userAuth)
                .path("course/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .delete()) {
            assertEquals(response.getStatus(), 403);
        }
    }
}