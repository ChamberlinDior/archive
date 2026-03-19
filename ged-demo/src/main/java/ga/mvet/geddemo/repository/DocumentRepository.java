package ga.mvet.geddemo.repository;

import ga.mvet.geddemo.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByReferenceCode(String referenceCode);

    Optional<Document> findByStoredFileName(String storedFileName);

    @Query("""
        select d
        from Document d
        left join fetch d.category
        left join fetch d.department
        order by d.id desc
    """)
    List<Document> findAllWithRelations();

    @Query("""
        select d
        from Document d
        left join fetch d.category
        left join fetch d.department
        where d.active = true
        order by d.id desc
    """)
    List<Document> findByActiveTrueWithRelations();

    @Query("""
        select d
        from Document d
        left join fetch d.category
        left join fetch d.department
        where lower(d.status) = lower(:status)
        order by d.id desc
    """)
    List<Document> findByStatusIgnoreCaseWithRelations(@Param("status") String status);

    @Query("""
        select d
        from Document d
        left join fetch d.category
        left join fetch d.department
        where lower(d.title) like lower(concat('%', :keyword, '%'))
        order by d.id desc
    """)
    List<Document> findByTitleContainingIgnoreCaseWithRelations(@Param("keyword") String keyword);

    @Query("""
        select d
        from Document d
        left join fetch d.category
        left join fetch d.department
        where d.id = :id
    """)
    Optional<Document> findByIdWithRelations(@Param("id") Long id);
}