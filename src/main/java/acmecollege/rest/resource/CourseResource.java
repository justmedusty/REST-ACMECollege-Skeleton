package acmecollege.rest.resource;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static acmecollege.utility.MyConstants.*;

@Path(COURSE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    //unsure if we really need this for courses
    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE,USER_ROLE})
    public Response getCourses() {
        LOG.debug("retrieving all courses ...");
        List<Course> course = service.getAllCourses();
        return Response.ok(course).build();
    }


    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteCourseById(int id) {
        LOG.debug("Deleting course with id = {}", id);
        service.deleteCourseById(id);
        return Response.ok(id).build();
    }


    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourse(Course newCourse) {
        Response response = null;
        Course course = service.persistCourse(newCourse);
        // Build a SecurityUser linked to the new student
        response = Response.ok(course).build();
        return response;
    }


}