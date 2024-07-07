package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 구간 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SectionAcceptanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @Transactional
    void setUp() {
        SectionTestSetup sectionTestSetup = new SectionTestSetup(jdbcTemplate);
        sectionTestSetup.setUpDatabase();
    }

    /**
     * Given 특정 노선이 등록돼있고
     * When 관리자가 해당 노선에 새로운 구간을 추가하면
     * Then 해당 노선에 새로운 구간이 추가된다.
     */
    @Test
    @DisplayName("노선에 새로운 구간 추가")
    void 구간등록_case1() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);
        ExtractableResponse<Response> lineCreationResponse = getLineCreationExtract(params);
        long lineId = lineCreationResponse.jsonPath().getLong("id");

        Map<String, Object> newSection = getSectionRequestParamMap(2L, 3L, 10L);


        // when
        ExtractableResponse<Response> response = getCreateNewLineExtract(newSection, lineId);
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        List<Map<String, Object>> stations = getLineExtract(lineId).jsonPath().getList("stations");
        assertThat(stations.size()).isEqualTo(3);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
        assertThat(stations.get(2).get("id")).isEqualTo(3);
    }

    /**
     * Given 특정 노선이 등록돼있고
     * When 관리자가 노선의 하행 종점역과 다른 상행역을 가진 구간을 추가하면
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("노선의 하행 종점역과 다른 상행역을 가진 구간 추가시 예외 발생")
    void 구간등록_case2() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);
        ExtractableResponse<Response> lineCreationResponse = getLineCreationExtract(params);
        long lineId = lineCreationResponse.jsonPath().getLong("id");

        Map<String, Object> newSection = getSectionRequestParamMap(3L, 4L, 10L);

        // when
        ExtractableResponse<Response> response = getCreateNewLineExtract(newSection, lineId);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        List<Map<String, Object>> stations = getLineExtract(lineId).jsonPath().getList("stations");
        assertThat(stations.size()).isEqualTo(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
    }

    /**
     * Given 특정 노선이 등록돼있고
     * When 관리자가 이미 노선에 등록된 역을 하행역으로 가진 새로운 구간을 추가하면
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("노선에 이미 등록된 역을 하행역으로 가진 구간 추가시 예외 발생")
    void 구간등록_case3() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);
        ExtractableResponse<Response> lineCreationResponse = getLineCreationExtract(params);
        long lineId = lineCreationResponse.jsonPath().getLong("id");

        Map<String, Object> newSection = getSectionRequestParamMap(2L, 1L, 10L);

        // when
        ExtractableResponse<Response> response = getCreateNewLineExtract(newSection, lineId);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        List<Map<String, Object>> stations = getLineExtract(lineId).jsonPath().getList("stations");
        assertThat(stations.size()).isEqualTo(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
    }

    /**
     * Given 노선이 등록돼있고,
     * When 관리자가 노선의 마지막 구간을 삭제하면
     * Then 노선에서 구간이 삭제된다.
     */
    @Test
    @DisplayName("노선의 마지막 구간 삭제")
    void 구간삭제_case1() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);
        ExtractableResponse<Response> lineCreationResponse = getLineCreationExtract(params);
        long lineId = lineCreationResponse.jsonPath().getLong("id");

        Map<String, Object> newSection = getSectionRequestParamMap(2L, 3L, 10L);

        getCreateNewLineExtract(newSection, lineId);

        // when
        ExtractableResponse<Response> response = getSectionDeletionExtract(lineId, 3L);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        List<Map<String, Object>> stations = getLineExtract(lineId).jsonPath().getList("stations");
        assertThat(stations.size()).isEqualTo(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
    }

    /**
     * Given 노선이 등록돼있고
     * When 관리자가 노선의 구간이 아닌 다른 구간을삭제하면
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("노선의 구간이 아닌 다른 구간 삭제 시 예외 발생")
    void 구간삭제_case2() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);
        ExtractableResponse<Response> lineCreationResponse = getLineCreationExtract(params);
        long lineId = lineCreationResponse.jsonPath().getLong("id");

        Map<String, Object> newSection = getSectionRequestParamMap(2L, 3L, 10L);

        getCreateNewLineExtract(newSection, lineId);

        // when
        ExtractableResponse<Response> response = getSectionDeletionExtract(lineId, 4L);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        List<Map<String, Object>> stations = getLineExtract(lineId).jsonPath().getList("stations");
        assertThat(stations.size()).isEqualTo(3);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
        assertThat(stations.get(2).get("id")).isEqualTo(3);
    }

    /**
     * Given 노선이 등록돼있고
     * When 관리자가 노선의 마지막 구간(하행종점역)이 아닌 다른 구간을 삭제하면
     * Then 에러가 발생하고 해당 구간은 삭제되지않는다.
     */
    @Test
    @DisplayName("노선의 마지막 구간(하행종점역)이 아닌 다른 구간 삭제 시 예외 발생")
    void 구간삭제_case3() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);
        ExtractableResponse<Response> lineCreationResponse = getLineCreationExtract(params);
        long lineId = lineCreationResponse.jsonPath().getLong("id");

        Map<String, Object> newSection = getSectionRequestParamMap(2L, 3L, 10L);

        getCreateNewLineExtract(newSection, lineId);

        // when
        ExtractableResponse<Response> response = getSectionDeletionExtract(lineId, 2L);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        List<Map<String, Object>> stations = getLineExtract(lineId).jsonPath().getList("stations");
        assertThat(stations.size()).isEqualTo(3);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
        assertThat(stations.get(2).get("id")).isEqualTo(3);
    }

    /**
     * Given 구간이 하나뿐인 노선이 등록돼있고
     * When 관리자가 해당 노선의 구간을 삭제하면
     * Then 에러가 발생하고 해당 구간은 삭제되지않는다.
     */
    @Test
    @DisplayName("노선에 구간이 하나뿐일때 구간 삭제 시 예외 발생")
    void 구간삭제_case4() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);
        ExtractableResponse<Response> lineCreationResponse = getLineCreationExtract(params);
        long lineId = lineCreationResponse.jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> response = getSectionDeletionExtract(lineId, 2L);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        List<Map<String, Object>> stations = getLineExtract(lineId).jsonPath().getList("stations");
        assertThat(stations.size()).isEqualTo(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
    }
    private Map<String, Object> getLineRequestParamMap(
            String name,
            String color,
            Long upStationId,
            Long downStationId,
            Long distance) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("color", color);
        params.put("upStationId", upStationId);
        params.put("downStationId", downStationId);
        params.put("distance", distance);
        return params;
    }

    private Map<String, Object> getSectionRequestParamMap(
            Long upStationId,
            Long downStationId,
            Long distance) {
        Map<String, Object> params = new HashMap<>();
        params.put("upStationId", upStationId);
        params.put("downStationId", downStationId);
        params.put("distance", distance);
        return params;
    }

    private ExtractableResponse<Response> getLineCreationExtract(Map<String, Object> params) {
        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract();
    }

    private ExtractableResponse<Response> getLineExtract(long lineId) {
        return RestAssured.given().log().all()
                .when().get("/lines/" + lineId)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> getCreateNewLineExtract(Map<String, Object> newSection, long lineId) {
        return RestAssured.given().log().all()
                .body(newSection)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines/" + lineId + "/sections")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> getSectionDeletionExtract(long lineId, long stationId) {
        return RestAssured.given().log().all()
                .queryParam("stationId", stationId)
                .when().delete("/lines/" + lineId + "/sections")
                .then().log().all()
                .extract();
    }

    private class SectionTestSetup extends DatabaseSetupTemplate {

        public SectionTestSetup(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        @Override
        protected void truncateTables() {
            jdbcTemplate.execute("TRUNCATE TABLE line");
            jdbcTemplate.execute("TRUNCATE TABLE station");
        }

        @Override
        protected void resetAutoIncrement() {
            jdbcTemplate.execute("ALTER TABLE station ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.execute("ALTER TABLE line ALTER COLUMN id RESTART WITH 1");
        }

        @Override
        protected void insertInitialData() {
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('신사역')");
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('강남역')");
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('판교역')");
        }
    }
}
