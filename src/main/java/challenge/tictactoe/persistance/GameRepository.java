package challenge.tictactoe.persistance;

import challenge.tictactoe.db.GameEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for 'games' table
 */
@Repository
public interface GameRepository extends ReactiveMongoRepository<GameEntity, String> {
    Mono<GameEntity> findById(String id);
}

