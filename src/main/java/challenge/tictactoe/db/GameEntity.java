package challenge.tictactoe.db;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "moves")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEntity {

    @Id
    private String id;
    private String status;
    private String winner;
    private String activeTurn;
    private String gameType;

}
