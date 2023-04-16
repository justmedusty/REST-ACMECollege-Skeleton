/***************************************************************************
 *
 * @author Evan Lin
 * @date April 16th 2023
 *
 * Updated by:  Group 2
 *   040862180, Dustyn, Gibb (as from ACSIS)
 *   041009257, Jian, Jiao (as from ACSIS)
 *   studentId, Mathew , Broeze (as from ACSIS)
 *   041023981, Evan, Lin (as from ACSIS)
 *
 */

package acmecollege;

import acmecollege.entity.ClubMembership;
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
public class ClubMembershipTests {

    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;
    static int record_id = 123;

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
    public void test01_getAllClubMemberships_with_adminrole() throws JsonMappingException{
        Response response = webTarget
                .register(adminAuth)
                .path("clubmembership")
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test02_getAllClubMemberships_with_userrole() throws JsonMappingException{
        Response response = webTarget
                .register(userAuth)
                .path("clubmembership")
                .request()
                .get();
        assertEquals(response.getStatus(), 403);
    }

    @Test
    public void test03_getClubMembershipById_with_adminrole() throws JsonMappingException{
        Response response = webTarget
                .register(adminAuth)
                .path("clubmembership/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test04_getClubMembershipById_with_userrole() throws JsonMappingException{
        Response response = webTarget
                .register(userAuth)
                .path("clubmembership/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .get();
        assertEquals(response.getStatus(), 200);
    }

    @Test
    public void test05_postClubMembership_with_adminrole() throws JsonMappingException{
        ClubMembership membership = new ClubMembership();
        membership.setId(123);
        try (Response response = webTarget
                .register(adminAuth)
                .path("clubmembership")
                .request()
                .post(Entity.json(membership))) {
            assertEquals(response.getStatus(), 200);
        }
    }

    @Test
    public void test06_postClubMembership_with_userrole() throws JsonMappingException{
        ClubMembership membership = new ClubMembership();
        membership.setId(123);
        try (Response response = webTarget
                .register(userAuth)
                .path("clubmembership")
                .request()
                .post(Entity.json(membership))) {
            assertEquals(response.getStatus(), 403);
        }
    }

    @Test
    public void test07_deleteClubMembership_with_adminrole() throws JsonMappingException{
        try (Response response = webTarget
                .register(adminAuth)
                .path("clubmembership/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .delete()) {
            assertEquals(response.getStatus(), 200);
        }
    }

    @Test
    public void test08_deleteClubMembership_with_userrole() throws JsonMappingException, JsonProcessingException {
        try (Response response = webTarget
                .register(userAuth)
                .path("clubmembership/{id}")
                .resolveTemplate("id", record_id)
                .request()
                .delete()) {
            assertEquals(response.getStatus(), 403);
        }
    }
}