package challenge.tictactoe.db;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "moves")
@CompoundIndex(name = "gameXY", unique = true, def = "{'gameId': 1, 'x' : 1, 'y': 1}")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveEntity {

    @Id
    private String id;
    private int number;
    private String playedBy;
    @Indexed
    private String gameId;
    private int x;
    private int y;
}
