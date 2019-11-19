package wonderland.security.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import wonderland.security.authentication.service.PermissionServices;
import wonderland.security.authentication.service.RoleServices;
import wonderland.security.authentication.service.UserServices;

import java.util.Set;

import static wonderland.security.authentication.service.UserServices.USER_ROLE_NAME;

@EnableAuthorizationServer
//@EnableWebSecurity
@SpringBootApplication
public class SecurityAuthenticationApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(SecurityAuthenticationApplication.class, args);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuthenticationApplication.class);

    @Autowired
    PermissionServices permissionServices;

    @Autowired
    RoleServices roleServices;

    @Autowired
    UserServices userService;

    @Override
    public void run(String... args) throws Exception {
        var permission = permissionServices.createAPermission("generic", "all");
        var role = roleServices.createARole(USER_ROLE_NAME, Set.of(permission));
        var user = userService.createNewUserAccount("bmd579@gmail.com", "09398240640", "bmd007", "Mahdi");
        var updatedUser = userService.addRoleToUser(user.getPhoneNumber(), role.getRoleName());
        LOGGER.info("User {} is added completely", updatedUser);
    }
}
