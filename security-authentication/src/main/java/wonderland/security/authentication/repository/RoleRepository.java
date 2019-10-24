package wonderland.security.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wonderland.security.authentication.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
