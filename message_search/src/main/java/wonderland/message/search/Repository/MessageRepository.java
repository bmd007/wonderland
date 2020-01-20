package wonderland.message.search.Repository;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import wonderland.message.search.domain.Message;

@Repository
public interface MessageRepository extends ReactiveElasticsearchRepository<Message, String> {
    Flux<Message> findBySender(String sender);

    Flux<Message> findByTextContaining(String text);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"messages.receiver\": \"?0\"}}]}}")
    Flux<Message> findByReceiver(String receiver);
}
