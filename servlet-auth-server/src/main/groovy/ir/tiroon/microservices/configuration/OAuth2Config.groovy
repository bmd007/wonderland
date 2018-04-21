package ir.tiroon.microservices.configuration

import ir.tiroon.microservices.service.OauthClientDetailServices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer

import java.security.Principal

@Configuration
@EnableAuthorizationServer
class OAuth2Config extends AuthorizationServerConfigurerAdapter {


    @Autowired
    PasswordEncoder passwordEncoder


    @Autowired
    OauthClientDetailServices clientDetailServices


    @Autowired
    @Qualifier("authenticationManagerBean")
    AuthenticationManager authenticationManager;

    @Override
    void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
                .tokenKeyAccess('permitAll()')
                .checkTokenAccess('isAuthenticated()')
                .passwordEncoder(passwordEncoder)
    }

    @Override
    void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        clients.withClientDetails(clientDetailServices).build()
//                .inMemory()
//                .withClient('person-command-client')
//                .secret('{bcrypt}\$2a\$10$ufhOWoGMo3ErYoqPCFIifuzRDTMXgK5xRRrcHekzGbuFqfYCCdur6')
//                .authorizedGrantTypes('refresh_token', 'password')
//                .scopes('openid')
//
//                .and()
    }

    @Override
    void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
    }


}
