package challenge.tictactoe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import lombok.*;

/**
 * Data Transfer Object for handling Move JSON objects via HTTP requests/response
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MoveDto {

    @NotNull
    int x;
    @NotNull
    int y;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    int number;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String playedBy;

}
