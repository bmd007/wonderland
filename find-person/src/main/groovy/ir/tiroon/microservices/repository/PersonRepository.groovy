package ir.tiroon.microservices.repository

import ir.tiroon.microservices.model.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository extends JpaRepository<Person, String> {
    List<Person> findByName(String name);
}

