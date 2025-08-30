package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Long itemId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.item.id IN :itemIds")
    List<Comment> findByItemIdIn(@Param("itemIds") List<Long> itemIds);
}
