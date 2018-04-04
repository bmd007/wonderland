package ir.tiroon.microservices.configuration

import ir.tiroon.microservices.service.OauthClientDetailServices
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer

@Configuration
@EnableAuthorizationServer
class OAuth2Config extends AuthorizationServerConfigurerAdapter {


    @Autowired
    PasswordEncoder passwordEncoder


    @Autowired
    OauthClientDetailServices clientDetailServices


    @Override
    void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .passwordEncoder(passwordEncoder)
    }

    @Override
    void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailServices).build()
//                .and()
//                .withClient("person-interest")
//                .secret("person-interest-secret")
//                .authorizedGrantTypes("refresh_token", "password")
//                .scopes("openid")
    }


    @Autowired
    AuthenticationManager authenticationManager


    @Override
    void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
    }

//    AuthenticationManager oauthAuthenticationManager(){
//        AuthenticationManager authenticationManager = new AuthenticationManager() {
//            @Override
//            Authentication authenticate(Authentication authentication) throws AuthenticationException {
//                System.out.println("BMD princepal  "+authentication.principal)
//                if (clientDetailServices.get(authentication.principal).secret
//                        ==
//                    passwordEncoder.encode(authentication.credentials)
//                )
//                return authentication
//            }
//        }
//    }
//
//    @Override
//    void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
//        endpoints.authenticationManager(oauthAuthenticationManager())
//    }


}
