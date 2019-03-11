package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*

class User implements Serializable {

    String phoneNumber

    String name

    String password

    String email

    Set<Role> roles = new HashSet<>()

    State state = State.Active

    Set<Note> collaborations = new HashSet<>()

    Set<Note> notes = new HashSet<>()

    Set<NoteChangedEvent> changes = new HashSet<>()

    User() {
    }

    @JsonCreator
    User(@JsonProperty("name") String name, @JsonProperty("password") String password,
         @JsonProperty("email") String email, @JsonProperty("phoneNumber") String phoneNumber) {
        this.name = name
        this.password = password
        this.email = email
        this.phoneNumber = phoneNumber
    }

    User(String phoneNumber, String name, String password, String email, Set<Role> roles, State state, Set<Note> collaborations, Set<Note> notes, Set<NoteChangedEvent> changes) {
        this.phoneNumber = phoneNumber
        this.name = name
        this.password = password
        this.email = email
        this.roles = roles
        this.state = state
        this.collaborations = collaborations
        this.notes = notes
        this.changes = changes
    }

    Set<Note> getNotes() {
        notes
    }

    void setNotes(Set<Note> notes) {
        this.notes = notes
    }

    Set<NoteChangedEvent> getChanges() {
        changes
    }

    void setChanges(Set<NoteChangedEvent> changes) {
        this.changes = changes
    }


    void collaborateIn(Note note) {
        collaborations.add(note)
    }

    Set<Note> getCollaborations() {
        collaborations
    }

    void setCollaborations(Set<Note> collaborations) {
        this.collaborations = collaborations
    }

    String getPhoneNumber() {
        phoneNumber
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber
    }

    String getName() {
        name
    }

    void setName(String name) {
        this.name = name
    }

    String getPassword() {
        password
    }

    void setPassword(String password) {
        this.password = password
    }

    String getEmail() {
        email
    }

    void setEmail(String email) {
        this.email = email
    }

    Set<Role> getRoles() {
        roles
    }

    void setRoles(Set<Role> roles) {
        this.roles = roles
    }

    State getState() {
        state
    }

    void setState(State state) {
        this.state = state
    }
}
