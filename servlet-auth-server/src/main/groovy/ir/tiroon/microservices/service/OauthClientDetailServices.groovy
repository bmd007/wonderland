package ir.tiroon.microservices.service

import ir.tiroon.microservices.model.oauth2.Oauth2Client
import ir.tiroon.microservices.repository.OauthClientRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.ClientRegistrationException
import org.springframework.security.oauth2.provider.client.BaseClientDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("oauthClientService")
@Transactional
class OauthClientDetailServices implements ClientDetailsService {

    @Autowired
    OauthClientRepository oauth2ClientRepository

    @Autowired
    PasswordEncoder passwordEncoder

    void persist(String id, String secret) {
        oauth2ClientRepository.save(new Oauth2Client(id, passwordEncoder.encode(secret)))
    }

    Oauth2Client get(String id) {
        oauth2ClientRepository.getOne(id)
    }

    ArrayList<Oauth2Client> getList() {
        (ArrayList<Oauth2Client>) oauth2ClientRepository.findAll()
    }

    void deleteById(String id) {
        oauth2ClientRepository.delete(get(id))
    }

    Collection<String> grant_types = ["refresh_token", "password"]
    Collection<String> scopes = ["openid"]


    @Transactional(readOnly = true)
    @Override
    ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        def client = get(clientId)
        ClientDetails clientDetails = new BaseClientDetails()
        clientDetails.setClientId(client.getId())
        clientDetails.setClientSecret(client.secret)
        clientDetails.setAuthorizedGrantTypes(grant_types)
        clientDetails.setScope(scopes)

        System.out.println("BMDD::" + clientDetails.scope.stream().count())
        System.out.println("BMDD::" + clientDetails.getAuthorizedGrantTypes().stream().count())

        client
    }


}
