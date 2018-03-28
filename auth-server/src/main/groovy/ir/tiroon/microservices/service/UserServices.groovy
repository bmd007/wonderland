package ir.tiroon.microservices.service

import ir.tiroon.microservices.model.userManagement.Role
import ir.tiroon.microservices.model.userManagement.State
import ir.tiroon.microservices.model.userManagement.User
import ir.tiroon.microservices.repository.RoleRepository
import ir.tiroon.microservices.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("userService")
@Transactional
class UserServices {

    @Autowired
    UserRepository userRepository

    @Autowired
    RoleRepository roleRepository

//    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    void persist(User user) {
        user.setState(State.Active)
        Optional<Role> byId = roleRepository.findById((long) 1)
        user.getRoles().add(byId.get())
        userRepository.save(user)
    }

    User get(String phn) {
        userRepository.getOne(phn)
    }

    ArrayList<User> getList() {
        (ArrayList<User>) userRepository.findAll()
    }

    void deleteById(String phn) {
        userRepository.delete(get(phn))
    }


}