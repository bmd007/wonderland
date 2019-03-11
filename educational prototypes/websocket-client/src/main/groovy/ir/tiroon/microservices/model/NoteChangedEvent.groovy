package ir.tiroon.microservices.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

import java.time.LocalDateTime

class NoteChangedEvent implements Serializable {

    Long changeId

    LocalDateTime time

    User by

    Note note

    NoteChangedEvent(User by, Note note) {
        this.time = LocalDateTime.now()
        this.by = by
        this.note = note
    }


    @JsonCreator
    NoteChangedEvent(@JsonProperty("note") Note note) {
        this.note = note
    }

    NoteChangedEvent() {
    }

    Long getChangeId() {
        return changeId
    }

    void setChangeId(Long changeId) {
        this.changeId = changeId
    }

    LocalDateTime getTime() {
        return time
    }

    void setTime(LocalDateTime time) {
        this.time = time
    }

    User getBy() {
        return by
    }

    void setBy(User by) {
        this.by = by
    }

    Note getNote() {
        return note
    }

    void setNote(Note note) {
        this.note = note
    }
}
