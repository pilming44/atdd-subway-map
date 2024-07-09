package subway.entity;

import subway.exception.IllegalSectionException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Line {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String color;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    protected Line() {
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Line(String name, String color, Section section) {
        this.name = name;
        this.color = color;
        this.sections.add(section);
        section.setLine(this);
    }

    public List<Station> getStations() {
        List<Station> stations = new ArrayList<>();

        if (sections.isEmpty()) {
            return stations;
        }

        Section currentSection = sections.get(0);
        stations.add(currentSection.getUpStation());
        stations.add(currentSection.getDownStation());

        Optional<Section> nextSection = findNextSection(currentSection);

        while (nextSection.isPresent()) {
            currentSection = nextSection.get();
            stations.add(currentSection.getDownStation());
            nextSection = findNextSection(currentSection);
        }

        return stations;
    }

    public void addSection(Section section) {

        validateStationLink(section);

        validateStationDuplication(section);

        sections.add(section);
    }

    public Section removedSection(Station downStation) {

        validateDeleteEmpty();

        validateDeleteOnlyOne();

        validateDeletableSection(downStation);

        Section removedSection = sections.get(sections.size() - 1);

        sections.remove(sections.size() - 1);

        return removedSection;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private Optional<Section> findNextSection(Section tempSection) {
        return sections.stream()
                .filter(section -> section.getUpStation().equals(tempSection.getDownStation()))
                .findFirst();
    }

    private void validateDeletableSection(Station downStation) {
        if (sections.isEmpty()) {
            return;
        }

        if (sections.get(sections.size() - 1).getDownStation().getId() != downStation.getId()) {
            throw new IllegalSectionException("노선의 마지막 구간이 아닙니다.");
        }
    }

    private void validateDeleteOnlyOne() {
        if (sections.size() == 1) {
            throw new IllegalSectionException("노선에 구간이 하나뿐이면 삭제할수없습니다.");
        }
    }

    private void validateDeleteEmpty() {
        if (sections.isEmpty()) {
            throw new IllegalSectionException("노선에 삭제 할 구간이 없습니다.");
        }
    }

    private void validateStationDuplication(Section section) {
        for (Section s : sections) {
            if (s.getUpStation().getId() == section.getDownStation().getId()
                    || s.getDownStation().getId() == section.getDownStation().getId()) {
                throw new IllegalSectionException("구간의 하행역이 이미 노선에 등록되어있는 역입니다.");
            }
        }
    }

    private void validateStationLink(Section section) {
        if (sections.isEmpty()) {
            return;
        }
        if (sections.get(sections.size() - 1).getDownStation().getId() != section.getUpStation().getId()) {
            throw new IllegalSectionException("구간의 상행역이 노선 마지막 하행종점역이 아닙니다.");
        }
    }
}
