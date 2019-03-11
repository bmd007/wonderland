package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class Note implements Serializable  {

    Long noteId
    String label
    String text
    User owner

    @JsonIgnore
    Set<User> collaborators = new HashSet<>()
    @JsonIgnore
    Set<NoteChangedEvent> changes = new HashSet<>();


    Note() {
    }

    Note(String label, String text, User owner, Set<User> collaborators) {
        this.label = label
        this.text = text
        this.owner = owner
        this.collaborators = collaborators
    }

    @JsonCreator
    Note(@JsonProperty("label") String label, @JsonProperty("text") String text,
         @JsonProperty("owner") User owner) {
        this.label = label
        this.text = text
        this.owner = owner
    }

    @JsonCreator
    Note(@JsonProperty String label, @JsonProperty String text) {
        this.label = label
        this.text = text
    }

    Set<NoteChangedEvent> getChanges() {
        return changes
    }

    void setChanges(Set<NoteChangedEvent> changes) {
        this.changes = changes
    }

    void addCollaborator(User user){
        collaborators.add(user)
    }

    void removeCollaborator(User user){
        collaborators.remove(user)
    }

    Long getNoteId() {
        return noteId
    }

    void setNoteId(Long id) {
        this.noteId = id
    }

    String getLabel() {
        return label
    }

    void setLabel(String label) {
        this.label = label
    }

    String getText() {
        return text
    }

    void setText(String text) {
        this.text = text
    }

    User getOwner() {
        return owner
    }

    void setOwner(User owner) {
        this.owner = owner
    }

    Set<User> getCollaborators() {
        return collaborators
    }

    void setCollaborators(Set<User> collaborators) {
        this.collaborators = collaborators
    }
}
