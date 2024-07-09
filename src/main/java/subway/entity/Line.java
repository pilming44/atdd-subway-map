package subway.entity;

import subway.exception.IllegalSectionException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
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

    private Optional<Section> findNextSection(Section tempSection) {
        return sections.stream()
                .filter(section -> section.getUpStation().equals(tempSection.getDownStation()))
                .findFirst();
    }

    public int getSectionCount() {
        return sections.size();
    }

    private Section getLastSection() {
        return sections.get(sections.size() - 1);
    }

    public void addSection(Section section) {

        validateStationLink(section);

        validateStationDuplication(section);

        sections.add(section);
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
        if (getSectionCount() != 0
                && (getLastSection().getDownStation().getId() != section.getUpStation().getId())) {
            throw new IllegalSectionException("구간의 상행역이 노선 마지막 하행종점역이 아닙니다.");
        }
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
}
