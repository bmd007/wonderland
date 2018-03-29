package ir.tiroon.microservices.configuration


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("customUserDetailsService")
    UserDetailsService userDetailsService

    @Autowired
    CustomSuccessHandler customSuccessHandler

    @Autowired
    PersistentTokenRepository tokenRepository

    @Bean
    AuthenticationManager authenticationManagerBean() throws Exception {
        super.authenticationManagerBean()
    }

//    @Bean
//    PasswordEncoder passwordEncoder() {
//        new BCryptPasswordEncoder()
//    }

    @Bean
    PasswordEncoder passwordEncoder() {
        PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    @Qualifier("authenticationProvider")
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService)
        authenticationProvider.setPasswordEncoder(passwordEncoder())
        authenticationProvider
    }

    @Autowired
    void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
        auth.authenticationProvider(authenticationProvider())
    }

    @Bean
    PersistentTokenBasedRememberMeServices getPersistentTokenBasedRememberMeServices() {
        PersistentTokenBasedRememberMeServices tokenBasedService = new PersistentTokenBasedRememberMeServices(
                "remember-me", userDetailsService, tokenRepository)
        tokenBasedService
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //notice
        //--> NEVER EVER Implement a servlet(POST) , rest(POST) on Address *'/login'*
        http
                .authorizeRequests()
                .antMatchers("/accessDenied", "/login", "/resources/**", "/oauth/check_token", "/oauth/token").permitAll()
                .antMatchers("/test", "/test_get/**", "/test_get").access("hasRole('ROLE_ADMIN')")
                .antMatchers("/index", "/test_post").access("hasRole('ROLE_USER')")
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .formLogin().loginPage("/login")
                .failureUrl("/login?error=1")
                .permitAll()
                .and().rememberMe().rememberMeParameter("remember-me").tokenRepository(tokenRepository).tokenValiditySeconds(86400)
                .and().logout().clearAuthentication(true).invalidateHttpSession(true)
                .and().exceptionHandling().accessDeniedPage("/accessDenied")
                .and().csrf().requireCsrfProtectionMatcher(new AntPathRequestMatcher("/login")).disable()
    }


}