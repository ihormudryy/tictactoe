package challenge.tictactoe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;

/**
 * Data Transfer Object for handling Game JSON objects via HTTP requests/response
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameDto {

    String id;
    ArrayList<MoveDto> moves;
    String status;
    String winner;
    String activeTurn;
    String gameType;
}
