/***************************************************************************
 * File:  Student.java Course materials (23W) CST 8277
 * 
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @date August 28, 2022
 *
 * Updated by:  Group 2
 *   040862180, Dustyn, Gibb (as from ACSIS)
 *   041009257, Jian, Jiao (as from ACSIS)
 *   studentId, Mathew , Broeze (as from ACSIS)
 *   041023981, Evan, Lin (as from ACSIS)
 *   
 */
package acmecollege.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("unused")

/**
 * The persistent class for the student database table.
 */
@Entity
@Table(name = "student")
@NamedQuery(name = Student.ALL_STUDENTS_QUERY_NAME, query = "SELECT s FROM Student s")
@NamedQuery(name = Student.QUERY_STUDENT_BY_ID, query = "SELECT s FROM Student s where s.id = :param1")
//No need for AttributeOverride as student id column is called id as well.
public class Student extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;

    public static final String ALL_STUDENTS_QUERY_NAME = "Student.findAll";
    public static final String QUERY_STUDENT_BY_ID = "Student.findAllByID";

    public Student() {
    	super();
    }
    
	@Basic(optional = false)
    @Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@Basic(optional = false)
    @Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	@OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "owner")
	private Set<MembershipCard> membershipCards = new HashSet<>();

	@OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "student")
	private Set<CourseRegistration> courseRegistrations = new HashSet<>();

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

    // Simplify JSON body, skip MembershipCards
    @JsonIgnore
    public Set<MembershipCard> getMembershipCards() {
		return membershipCards;
	}

	public void setMembershipCards(Set<MembershipCard> membershipCards) {
		this.membershipCards = membershipCards;
	}

    // Simplify JSON body, skip CourseRegistrations
    @JsonIgnore
    public Set<CourseRegistration> getCourseRegistrations() {
		return courseRegistrations;
	}

	public void setCourseRegistrations(Set<CourseRegistration> courseRegistrations) {
		this.courseRegistrations = courseRegistrations;
	}

	public void setFullName(String firstName, String lastName) {
		setFirstName(firstName);
		setLastName(lastName);
	}
	
	//Inherited hashCode/equals is sufficient for this entity class

}
