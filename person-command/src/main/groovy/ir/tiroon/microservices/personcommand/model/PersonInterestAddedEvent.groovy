package ir.tiroon.microservices.personcommand.model

class PersonInterestAddedEvent extends Event{

    final String InterestName;

    PersonInterestAddedEvent(String relatedPersonPhoneNumber, String interestName) {
        super(EventType.InterestAdded.toString(), relatedPersonPhoneNumber)
        InterestName = interestName
    }

}
