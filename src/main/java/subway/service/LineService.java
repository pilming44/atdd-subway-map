package subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.dto.LineRequest;
import subway.dto.LineResponse;
import subway.entity.Line;
import subway.entity.Station;
import subway.repository.LineRepository;
import subway.repository.StationRepository;

import java.util.ArrayList;
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
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 역입니다."));;

        Line line = lineRepository.save(new Line(lineRequest.getName(), lineRequest.getColor(), upStation, downStation));
        return LineResponse.from(line);
    }

    public List<LineResponse> getAllLines() {
        List<Line> allLines = lineRepository.findAll();
        return allLines.stream()
                .map(line -> LineResponse.from(line))
                .collect(Collectors.toList());
    }
}
