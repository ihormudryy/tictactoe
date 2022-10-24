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
public class MoveEntity {

    @Id
    private String id;
    private int number;
    private String playedBy;
    private String gameId;
    private int x;
    private int y;
}
