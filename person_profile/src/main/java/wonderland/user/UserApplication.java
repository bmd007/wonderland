package wonderland.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import wonderland.user.service.PermissionServices;
import wonderland.user.service.RoleServices;
import wonderland.user.service.UserServices;

import java.util.Set;

@SpringBootApplication
public class UserApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserApplication.class);

    @Autowired
    PermissionServices permissionServices;

    @Autowired
    RoleServices roleServices;

    @Autowired
    UserServices userService;

    @Override
    public void run(String... args) {
        var permission = permissionServices.createAPermission("generic", "all");
        var role = roleServices.createARole(UserServices.USER_ROLE_NAME, Set.of(permission));
        var user = userService.createNewUserAccount("bmd579@gmail.com", "09398240640", "bmd007", "Mahdi");
        var updatedUser = userService.addRoleToUser(user.getPhoneNumber(), role.getRoleName());
        LOGGER.info("User {} is added completely", updatedUser);
    }
}
