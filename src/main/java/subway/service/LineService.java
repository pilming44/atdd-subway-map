package subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.dto.LineRequest;
import subway.dto.LineResponse;
import subway.dto.SectionRequest;
import subway.entity.Line;
import subway.entity.Section;
import subway.entity.Station;
import subway.exception.NoSuchLineException;
import subway.exception.NoSuchStationException;
import subway.repository.LineRepository;
import subway.repository.SectionRepository;
import subway.repository.StationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final SectionRepository sectionRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository, SectionRepository sectionRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Station upStation = null;
        Station downStation = null;

        if (lineRequest.getUpStationId() != null) {
            upStation = stationRepository.findById(lineRequest.getUpStationId())
                    .orElseThrow(()->new NoSuchStationException("존재하지 않는 역입니다."));
        }
        if (lineRequest.getDownStationId() != null) {
            downStation = stationRepository.findById(lineRequest.getDownStationId())
                    .orElseThrow(()->new NoSuchStationException("존재하지 않는 역입니다."));
        }
        Section section = sectionRepository.save(new Section(null, upStation, downStation, lineRequest.getDistance()));
        Line line = lineRepository.save(new Line(lineRequest.getName(), lineRequest.getColor()));
        line.addSection(section);
        section.setLine(line);

        return LineResponse.from(line);
    }

    public List<LineResponse> findAllLines() {
        List<Line> allLines = lineRepository.findAll();
        return allLines.stream()
                .map(line -> LineResponse.from(line))
                .collect(Collectors.toList());
    }

    public LineResponse findLine(Long id) {
        return LineResponse.from(lineRepository.findById(id)
                .orElseThrow(()->new NoSuchLineException("존재하지 않는 노선입니다.")));
    }

    @Transactional
    public void updateLine(Long id, LineRequest lineRequest) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new NoSuchLineException("존재하지 않는 노선입니다."));
        if (lineRequest.getName() != null) {
            line.setName(lineRequest.getName());
        }
        if (lineRequest.getColor() != null) {
            line.setColor(lineRequest.getColor());
        }
    }

    @Transactional
    public void removeLine(Long id) {
        lineRepository.deleteById(id);
    }

    @Transactional
    public LineResponse addSection(Long id, SectionRequest sectionRequest) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new NoSuchLineException("존재하지 않는 노선입니다."));
        Station upStation = stationRepository.findById(sectionRequest.getUpStationId())
                .orElseThrow(()->new NoSuchStationException("존재하지 않는 역입니다."));

        Station downStation = stationRepository.findById(sectionRequest.getDownStationId())
                .orElseThrow(()->new NoSuchStationException("존재하지 않는 역입니다."));

        Section section = new Section(line, upStation, downStation, sectionRequest.getDistance());

        line.addSection(section);

        sectionRepository.save(section);

        return LineResponse.from(line);
    }

    @Transactional
    public void removeSection(Long id, Long stationId) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new NoSuchLineException("존재하지 않는 노선입니다."));
        Station downStation = stationRepository.findById(stationId)
                .orElseThrow(()->new NoSuchStationException("존재하지 않는 역입니다."));

        Section removedSection = line.removedSection(downStation);

        sectionRepository.delete(removedSection);
    }
}
