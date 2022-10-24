package challenge.tictactoe.persistance;

import challenge.tictactoe.db.MoveEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for 'moves' table
 */
@Repository
public interface MoveRepository extends ReactiveMongoRepository<MoveEntity, String> {
    Mono<MoveEntity> findById(String id);

    Flux<MoveEntity> findByGameId(String gameId);
}
