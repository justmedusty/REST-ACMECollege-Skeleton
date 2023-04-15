/**
 * File:  ACMEColegeService.java
 * Course materials (23W) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * <p>
 * Updated by:  Group NN
 * studentId, firstName, lastName (as from ACSIS)
 * studentId, firstName, lastName (as from ACSIS)
 * studentId, firstName, lastName (as from ACSIS)
 * studentId, firstName, lastName (as from ACSIS)
 */
package acmecollege.ejb;

import static acmecollege.entity.StudentClub.SPECIFIC_STUDENT_CLUB_QUERY_NAME;
import static acmecollege.entity.StudentClub.IS_DUPLICATE_QUERY_NAME;
import static acmecollege.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmecollege.utility.MyConstants.PARAM1;
import static acmecollege.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmecollege.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmecollege.utility.MyConstants.PU_NAME;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;

import acmecollege.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")

/*
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger();

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;

    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Student> getAllStudents() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        cq.select(cq.from(Student.class));
        return em.createQuery(cq).getResultList();
    }

    public Student getStudentById(int id) {
        return em.find(Student.class, id);
    }

    @Transactional
    public Student persistStudent(Student newStudent) {
        em.persist(newStudent);
        return newStudent;
    }

    /**
     * To delete a student by id
     *
     * @param id - student id to delete
     */
    @Transactional
    public void deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            em.refresh(student);
            TypedQuery<SecurityUser> findUser =
                /* TODO ACMECS02 - Use NamedQuery on SecurityRole to find this related Student
                   so that when we remove it, the relationship from SECURITY_USER table
                   is not dangling
                */   em.createNamedQuery(SecurityUser.USER_FOR_OWNING_STUDENT_QUERY, SecurityUser.class)
                    .setParameter("param1", student.getId());
            SecurityUser sUser = findUser.getSingleResult();
            em.remove(sUser);
            em.remove(student);
        }
    }

    @Transactional
    public void buildUserForNewStudent(Student newStudent) {
        SecurityUser userForNewStudent = new SecurityUser();
        userForNewStudent.setUsername(
                DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewStudent.setPwHash(pwHash);
        userForNewStudent.setStudent(newStudent);
        /* TODO ACMECS01 - Use NamedQuery on SecurityRole to find USER_ROLE */
        SecurityRole userRole = em.createNamedQuery(SecurityRole.FIND_USER_ROLE, SecurityRole.class)
                .setParameter("param1", "USER_ROLE")
                .getSingleResult();
        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);
        em.persist(userForNewStudent);
    }

    @Transactional
    public Professor setProfessorForStudentCourse(int studentId, int courseId, Professor newProfessor) {
        Student studentToBeUpdated = em.find(Student.class, studentId);
        if (studentToBeUpdated != null) { // Student exists
            Set<CourseRegistration> courseRegistrations = studentToBeUpdated.getCourseRegistrations();
            courseRegistrations.forEach(c -> {
                if (c.getCourse().getId() == courseId) {
                    if (c.getProfessor() != null) { // Professor exists
                        Professor prof = em.find(Professor.class, c.getProfessor().getId());
                        prof.setProfessor(newProfessor.getFirstName(),
                                newProfessor.getLastName(),
                                newProfessor.getDepartment());
                        em.merge(prof);
                    } else { // Professor does not exist
                        c.setProfessor(newProfessor);
                        em.merge(studentToBeUpdated);
                    }
                }
            });
            return newProfessor;
        } else return null;  // Student doesn't exists
    }

    /**
     * To update a student
     *
     * @param id                 - id of entity to update
     * @param studentWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Student updateStudentById(int id, Student studentWithUpdates) {
        Student studentToBeUpdated = getStudentById(id);
        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            em.merge(studentWithUpdates);
            em.flush();
        }
        return studentToBeUpdated;
    }


    public List<StudentClub> getAllStudentClubs() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<StudentClub> cq = cb.createQuery(StudentClub.class);
        cq.select(cq.from(StudentClub.class));
        return em.createQuery(cq).getResultList();
    }

    // Why not use the build-in em.find?  The named query SPECIFIC_STUDENT_CLUB_QUERY_NAME
    // includes JOIN FETCH that we cannot add to the above API
    public StudentClub getStudentClubById(int id) {
        TypedQuery<StudentClub> specificStudentClubQuery = em.createNamedQuery(SPECIFIC_STUDENT_CLUB_QUERY_NAME, StudentClub.class);
        specificStudentClubQuery.setParameter(PARAM1, id);
        return specificStudentClubQuery.getSingleResult();
    }

    // These methods are more generic.

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        return allQuery.getResultList();
    }

    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> allQuery = em.createNamedQuery(namedQuery, entity);
        allQuery.setParameter(PARAM1, id);
        return allQuery.getSingleResult();
    }

    @Transactional
    public StudentClub deleteStudentClub(int id) {
        //StudentClub sc = getStudentClubById(id);
        StudentClub sc = getById(StudentClub.class, StudentClub.SPECIFIC_STUDENT_CLUB_QUERY_NAME, id);
        if (sc != null) {
            Set<ClubMembership> memberships = sc.getClubMemberships();
            List<ClubMembership> list = new LinkedList<>();
            memberships.forEach(list::add);
            list.forEach(m -> {
                if (m.getCard() != null) {
                    MembershipCard mc = getById(MembershipCard.class, MembershipCard.ID_CARD_QUERY_NAME, m.getCard().getId());
                    mc.setClubMembership(null);
                }
                m.setCard(null);
                em.merge(m);
            });
            em.remove(sc);
            return sc;
        }
        return null;
    }

    // Please study & use the methods below in your test suites

    public boolean isDuplicated(StudentClub newStudentClub) {
        TypedQuery<Long> allStudentClubsQuery = em.createNamedQuery(IS_DUPLICATE_QUERY_NAME, Long.class);
        allStudentClubsQuery.setParameter(PARAM1, newStudentClub.getName());
        return (allStudentClubsQuery.getSingleResult() >= 1);
    }

    @Transactional
    public StudentClub persistStudentClub(StudentClub newStudentClub) {
        em.persist(newStudentClub);
        return newStudentClub;
    }

    @Transactional
    public StudentClub updateStudentClub(int id, StudentClub updatingStudentClub) {
        StudentClub studentClubToBeUpdated = getStudentClubById(id);
        if (studentClubToBeUpdated != null) {
            em.refresh(studentClubToBeUpdated);
            studentClubToBeUpdated.setName(updatingStudentClub.getName());
            em.merge(studentClubToBeUpdated);
            em.flush();
        }
        return studentClubToBeUpdated;
    }

    @Transactional
    public ClubMembership persistClubMembership(ClubMembership newClubMembership) {
        em.persist(newClubMembership);
        return newClubMembership;
    }

    public ClubMembership getClubMembershipById(int cmId) {
        TypedQuery<ClubMembership> allClubMembershipQuery = em.createNamedQuery(ClubMembership.FIND_BY_ID, ClubMembership.class);
        allClubMembershipQuery.setParameter(PARAM1, cmId);
        return allClubMembershipQuery.getSingleResult();
    }

    @Transactional
    public ClubMembership updateClubMembership(int id, ClubMembership clubMembershipWithUpdates) {
        ClubMembership clubMembershipToBeUpdated = getClubMembershipById(id);
        if (clubMembershipToBeUpdated != null) {
            em.refresh(clubMembershipToBeUpdated);
            em.merge(clubMembershipWithUpdates);
            em.flush();
        }
        return clubMembershipToBeUpdated;
    }


    //Dustyns new code start
    //FOR COURSE----------------------------------------------------------------------------------------------------------------------

    @Transactional
    public void deleteCourseById(int courseId) {
        Course course = getById(Course.class, Course.COURSE_BY_ID, courseId);
        if (course != null) {
            em.remove(course);
        }
    }

    @Transactional
    public Course persistCourse(Course newCourse) {
        em.persist(newCourse);
        return newCourse;
    }

    public List<Course> getAllCourses() {
        TypedQuery<Course> allCoursesQuery = em.createNamedQuery(Course.ALL_COURSES_QUERY, Course.class);
        return allCoursesQuery.getResultList();
    }

    public Course getCourseById(int courseId) {
        TypedQuery<Course> courseByIdQuery = em.createNamedQuery(Course.COURSE_BY_ID, Course.class);
        courseByIdQuery.setParameter("param1", courseId);
        List<Course> courses = courseByIdQuery.getResultList();
        return courses.isEmpty() ? null : courses.get(0);
    }


    //FOR MEMBERSHIP CARD---------------------------------------------------------------------------------------------------------------------
    @Transactional
    public void deleteCardById(int cardId) {
        MembershipCard card = getById(MembershipCard.class, MembershipCard.ID_CARD_QUERY_NAME, cardId);
        if (card != null) {
            em.remove(card);
        }

    }

    @Transactional
    public MembershipCard persistCard(MembershipCard newCard) {
        em.persist(newCard);
        return newCard;
    }

    public List<MembershipCard> getAllCards() {
        TypedQuery<MembershipCard> allCardsQuery = em.createNamedQuery(MembershipCard.ALL_CARDS_QUERY_NAME, MembershipCard.class);
        return allCardsQuery.getResultList();
    }

    public MembershipCard getCardById(int membershipId){
        TypedQuery<MembershipCard> idQuery = em.createNamedQuery(MembershipCard.ID_CARD_QUERY_NAME , MembershipCard.class);
        idQuery.setParameter(PARAM1,membershipId);
        MembershipCard cardById = idQuery.getSingleResult();
        return cardById;

    }

    //Jians Stuff
    //---------------------------------For Course Registration Resource---------------------------------------------------------
    public List<CourseRegistration> getAllRegistration() {
        TypedQuery<CourseRegistration> allCardsQuery = em.createNamedQuery("CourseRegistration.findAll", CourseRegistration.class);
        return allCardsQuery.getResultList();
    }

    @Transactional
    public CourseRegistration persistCourseRegistration(CourseRegistration newCourseRegistration) {
        em.persist(newCourseRegistration);
        return newCourseRegistration;
    }

    public CourseRegistration getCourseRegistrationById(int studentId, int courseId) {
        TypedQuery<CourseRegistration> allQuery = em.createNamedQuery("CourseRegistration.findById", CourseRegistration.class);
        allQuery.setParameter(PARAM1, studentId);
        allQuery.setParameter("param2", courseId);
        CourseRegistration resultCourseRegistration = allQuery.getSingleResult();
        return resultCourseRegistration;
    }

    @Transactional
    public void deleteCourseRegistrationById(int studentId, int courseId) {

        CourseRegistration resultCourseRegistration = getCourseRegistrationById(studentId, courseId);
        if (resultCourseRegistration != null) {
            em.remove(resultCourseRegistration);
        }
    }

    //---------------------------------For Club Membership Resource---------------------------------------------------------
    public List<ClubMembership> getAllClubMembership() {
        TypedQuery<ClubMembership> allCardsQuery = em.createNamedQuery(ClubMembership.FIND_BY_ID, ClubMembership.class);
        return allCardsQuery.getResultList();
    }

    @Transactional
    public void deleteClubMembershipById(int id) {

        ClubMembership newClubMembership = getById(ClubMembership.class, ClubMembership.FIND_BY_ID, id);
        if (newClubMembership != null) {
            em.remove(newClubMembership);
        }
    }

    //-----------------------------------------------FOR PROFESSOR-----------------------------------------------------
    @Transactional
    public void deleteProfessorById(int professorId) {
        Professor professor = getById(Professor.class, Professor.PROFESSOR_BY_ID, professorId);
        if (professor != null) {
            em.remove(professor);
        }
    }

    @Transactional
    public void persistProfessor(Professor newProfessor) {
        em.persist(newProfessor);
    }

    public List<Professor> getAllProfessors() {
        TypedQuery<Professor> allProfessorsQuery = em.createNamedQuery(Professor.ALL_PROFESSORS_QUERY, Professor.class);
        return allProfessorsQuery.getResultList();
    }

    public Professor getProfessorById(int professorId) {
        TypedQuery<Professor> professorByIdQuery = em.createNamedQuery(Professor.PROFESSOR_BY_ID, Professor.class);
        professorByIdQuery.setParameter("param1", professorId);
        List<Professor> professors = professorByIdQuery.getResultList();
        return professors.isEmpty() ? null : professors.get(0);
    }

}