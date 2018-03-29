package ir.tiroon.microservices.configuration;



//@Configuration
//@EnableAuthorizationServer
public class OAuth2Config{
//    extends AuthorizationServerConfigurerAdapter {

//
//    @Autowired
//    @Qualifier("authenticationManagerBean")
//    private AuthenticationManager authenticationManager;
//
//
//
//    @Autowired(required = true)
//    JedisConnectionFactory redisConnectionFactory;
//
//    @Override
//    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
//        endpoints.authenticationManager(authenticationManager).tokenStore(new RedisTokenStore(redisConnectionFactory));
//    }
//
//
//    @Override
//    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
//        oauthServer
//                .tokenKeyAccess("permitAll()")
//                .checkTokenAccess("isAuthenticated()");
//    }
//
//
//
//    @Override
//    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory()
//                .withClient("homebrain")
//                .secret("homesecret")
//                .authorizedGrantTypes("refresh_token", "password")
//                .scopes("openid")
//
//            .and()
//                .withClient("homebrain2")
//                .secret("homesecret2")
//                .authorizedGrantTypes("refresh_token", "password")
//                .scopes("openid")
//
//            .and()
//                .withClient("sumprovider")
//                .secret("sumprovidersecret")
//                .authorizedGrantTypes("refresh_token", "password")
//                .scopes("openid");
//     }

}
