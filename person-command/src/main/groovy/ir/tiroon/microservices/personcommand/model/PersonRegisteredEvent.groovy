package ir.tiroon.microservices.personcommand.model

class PersonRegisteredEvent extends Event {

    final String relatedPersonName;

    PersonRegisteredEvent(String relatedPersonPhoneNumber, String relatedPersonName) {
        super(EventType.Registered.toString(), relatedPersonPhoneNumber)
        this.relatedPersonName = relatedPersonName
    }

}