package ir.tiroon.microservices.repository

import ir.tiroon.microservices.model.userManagement.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository extends JpaRepository<User, String> {
}
