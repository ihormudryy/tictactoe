package challenge.tictactoe.mapper;

import challenge.tictactoe.db.MoveEntity;
import challenge.tictactoe.dto.MoveDto;
import org.mapstruct.Mapper;

/**
 * Tictactoe game move mapper class for mapping objects between
 * DB entry to DTO and vise versa.
 */
@Mapper(componentModel = "spring")
public interface MoveMapper {
    MoveEntity dtoToEntity(MoveDto entity);

    MoveDto entityToDto(MoveEntity api);
}
