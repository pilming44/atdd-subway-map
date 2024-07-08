package subway.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import subway.entity.Line;

import java.util.List;
import java.util.Optional;

public interface LineRepository extends JpaRepository<Line, Long> {
    @Override
    @EntityGraph(attributePaths = {"sections", "sections.upStation", "sections.downStation"})
    List<Line> findAll();

    @Override
    @EntityGraph(attributePaths = {"sections", "sections.upStation", "sections.downStation"})
    Optional<Line> findById(Long id);
}
