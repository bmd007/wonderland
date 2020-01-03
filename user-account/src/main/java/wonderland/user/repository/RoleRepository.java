package wonderland.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wonderland.user.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
