package ir.tiroon.microservices.service

import ir.tiroon.microservices.model.userManagement.Role
import ir.tiroon.microservices.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("roleService")
@Transactional
class RoleServices {

    @Autowired
    RoleRepository roleRepository

    void save(Role role) { roleRepository.save(role) }

    Role get(Long id) {
        roleRepository.getOne(id)
    }

    ArrayList<Role> list() {
        (ArrayList<Role>) roleRepository.findAll()
    }

    void deleteById(int id) {
        roleRepository.delete(get((long) id))
    }

    void delete(Role role) {
        roleRepository.delete(role)
    }

}