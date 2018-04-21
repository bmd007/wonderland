package ir.tiroon.microservices.repository

import ir.tiroon.microservices.model.userManagement.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository extends JpaRepository<Role, String> {
    Role findRoleByRoleName(String roleName)
}
