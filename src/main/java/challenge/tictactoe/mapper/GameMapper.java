package challenge.tictactoe.mapper;

import challenge.tictactoe.db.GameEntity;
import challenge.tictactoe.dto.GameDto;
import org.mapstruct.Mapper;

/**
 * Game mapper class for mapping objects between
 * DB entry to DTO and vise versa.
 */
@Mapper(componentModel = "spring")
public interface GameMapper {

    GameEntity dtoToEntity(GameDto entity);

    GameDto dtoToEntity(GameEntity api);

}
