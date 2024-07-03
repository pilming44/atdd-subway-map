package subway.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import subway.entity.Line;

import java.util.List;
import java.util.Optional;

public interface LineRepository extends JpaRepository<Line, Long> {
    @Override
    @EntityGraph(attributePaths = {"upStation", "downStation"})
    List<Line> findAll();

    @Override
    @EntityGraph(attributePaths = {"upStation", "downStation"})
    Optional<Line> findById(Long id);

}
