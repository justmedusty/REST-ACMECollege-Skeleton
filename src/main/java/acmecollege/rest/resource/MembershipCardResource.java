package acmecollege.rest.resource;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.MembershipCard;
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
//dustyn
@Path(MEMBERSHIP_CARD_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MembershipCardResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    //unsure if we really need this for courses
    @Inject
    protected SecurityContext sc;


    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getMembershipCards() {
        LOG.debug("retrieving all courses ...");
        List<MembershipCard> card = service.getAllCards();
        return Response.ok(card).build();
    }


    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addMembershipCard(MembershipCard newCard) {
        Response response = null;
        MembershipCard card = service.persistCard(newCard);
        // Build a SecurityUser linked to the new student
        response = Response.ok(card).build();
        return response;
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteCardById(int id) {
        LOG.debug("Deleting course with id = {}", id);
        service.deleteCardById(id);
        return Response.ok(id).build();
    }


}
