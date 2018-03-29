package ir.tiroon.microservices.repository

import ir.tiroon.microservices.model.oauth2.Oauth2Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OauthClientRepository extends JpaRepository<Oauth2Client, String> {
}
