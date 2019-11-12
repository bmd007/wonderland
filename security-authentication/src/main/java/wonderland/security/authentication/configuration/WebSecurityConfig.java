package wonderland.security.authentication.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {//extends WebSecurityConfigurerAdapter {
//    @Autowired
//    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

//    @Bean
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
//
//    @Bean
//    @Qualifier("authenticationProvider")
//    public DaoAuthenticationProvider authenticationProvider() {
//        var authenticationProvider = new DaoAuthenticationProvider();
//        authenticationProvider.setUserDetailsService(userDetailsService);
//        authenticationProvider.setPasswordEncoder(passwordEncoder());
//        return authenticationProvider;
//    }
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService);
//        auth.authenticationProvider(authenticationProvider());
//    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        //notice
//        //--> NEVER EVER Implement a servlet(POST) , rest(POST) on Address *'/login'*
//        http
//                .authorizeRequests()
//                .antMatchers("/login", "oauth/token", "/javax.faces.resource/**", "/logout", "/resources/**").permitAll()
//                .anyRequest().authenticated();
////                .and().httpBasic()
////                .and().formLogin().loginPage("/login").failureUrl("/login?error=1")
////                .defaultSuccessUrl("/view/main.html",false).permitAll()
////                .and().exceptionHandling().accessDeniedPage("/accessDenied")
////                .and().logout().clearAuthentication(true).invalidateHttpSession(true).logoutUrl("/logout");
//
//    }
//
//    public UserDetailsService getUserDetailsService() {
//        return userDetailsService;
//    }
//
//    public void setUserDetailsService(UserDetailsService userDetailsService) {
//        this.userDetailsService = userDetailsService;
//    }
}
