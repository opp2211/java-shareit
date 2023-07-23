package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(value = " SELECT it " +
            "FROM Item it " +
            "JOIN FETCH it.owner ow " +
            "WHERE ow.id = ?1 " +
            "ORDER BY it.id",
            countQuery = "SELECT count(*) " +
                    "FROM Item it " +
                    "JOIN it.owner ow " +
                    "WHERE ow.id = ?1 ")
    Page<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    @Query(" select it from Item it " +
            "where it.available = true " +
            "AND( upper(it.name) like upper(concat('%', ?1, '%')) " +
            " or upper(it.description) like upper(concat('%', ?1, '%')))")
    Page<Item> searchAvailByText(String text, Pageable pageable);

    List<Item> findAllByRequestId(Long requestId);

    List<Item> findAllByRequestIdNotNull();
}
