package ir.tiroon.microservices.configuration

import cz.jirutka.spring.embedmongo.EmbeddedMongoFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.*;
import com.mongodb.MongoClient;

//TODO uncomment if didn't work with normal stuff
//@Configuration
class MongoConfig {

    private static final String MONGO_DB_URL = "localhost"
    private static final String MONGO_DB_NAME = "person-interest"
//    @Bean
    MongoTemplate mongoTemplate() {
        EmbeddedMongoFactoryBean mongo = new EmbeddedMongoFactoryBean()
        mongo.setBindIp(MONGO_DB_URL)
        MongoClient mongoClient = mongo.getObject()
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, MONGO_DB_NAME)
        return mongoTemplate
    }
}