package subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.dto.LineRequest;
import subway.dto.LineResponse;
import subway.entity.Line;
import subway.entity.Station;
import subway.repository.LineRepository;
import subway.repository.StationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineService {
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public LineResponse saveLine(LineRequest lineRequest) {
        Station upStation = stationRepository.findById(lineRequest.getUpStationId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 역입니다."));
        Station downStation = stationRepository.findById(lineRequest.getDownStationId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 역입니다."));

        Line line = lineRepository.save(new Line(lineRequest.getName(), lineRequest.getColor(), upStation, downStation));
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
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 역입니다.")));
    }

    @Transactional
    public void updateLine(Long id, LineRequest lineRequest) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역입니다."));
        if (lineRequest.getName() != null) {
            line.setName(lineRequest.getName());
        }
        if (lineRequest.getColor() != null) {
            line.setColor(lineRequest.getColor());
        }
        if(lineRequest.getUpStationId() != null) {
            Station upStation = stationRepository.findById(lineRequest.getUpStationId())
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 역입니다."));
            line.setUpStation(upStation);
        }
        if(lineRequest.getUpStationId() != null) {
            Station downStation = stationRepository.findById(lineRequest.getDownStationId())
                    .orElseThrow(()->new IllegalArgumentException("존재하지 않는 역입니다."));
            line.setDownStation(downStation);
        }
        if (lineRequest.getDistance() != null) {
            line.setDistance(lineRequest.getDistance());
        }
    }
}
