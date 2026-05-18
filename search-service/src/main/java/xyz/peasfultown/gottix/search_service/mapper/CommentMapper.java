package xyz.peasfultown.gottix.search_service.mapper;

import org.mapstruct.Mapper;
import xyz.peasfultown.gottix.search_service.entity.CommentDocument;
import xyz.peasfultown.gottix.search_service.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toModel(CommentDocument document);
}
