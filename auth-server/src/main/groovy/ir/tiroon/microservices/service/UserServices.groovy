package ir.tiroon.microservices.service

import ir.tiroon.microservices.model.userManagement.State
import ir.tiroon.microservices.model.userManagement.User
import ir.tiroon.microservices.repository.RoleRepository
import ir.tiroon.microservices.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("userService")
@Transactional
class UserServices {

    @Autowired
    UserRepository userRepository

    @Autowired
    RoleRepository roleRepository


    @Autowired
    PasswordEncoder passwordEncoder

    User saveWithPassEncoding(User user) {
        user.state = State.Active

        def userRole = roleRepository.findRoleByRoleName("USER")
        user.getRoles().add(userRole)
        user.setPassword(passwordEncoder.encode(user.getPassword()))
        userRepository.save(user)
        user
    }

    User saveWithParameter(String email, String name, String password, String phoneNumber){
        saveWithPassEncoding(new User(name, password, email, phoneNumber))
    }


    User get(String phn) {
            userRepository.getOne(phn)
    }

    User getAndNullIfNotExists(String phn) {
            userRepository.findUserByPhoneNumber(phn)
    }


    User getByEmail(String email) {
        userRepository.findUserByEmail(email)
    }

    ArrayList<User> list() {
        (ArrayList<User>) userRepository.findAll()
    }

    void delete(String phn) {
        userRepository.delete(get(phn))
    }

    void delete(User user) {
        userRepository.delete(user)
    }


}