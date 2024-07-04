package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LineAcceptanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @Transactional
    void setUp() {
        LineAndStationSetup lineAndStationSetup = new LineAndStationSetup(jdbcTemplate);
        lineAndStationSetup.setUpDatabase();
    }

    @Test
    @DisplayName("지하철 노선을 생성한다")
    void createLine() {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("name", "신분당선");
        params.put("color", "bg-red-600");
        params.put("upStationId", 1);
        params.put("downStationId", 2);
        params.put("distance", 10);

        // when
        ExtractableResponse<Response> response = getCreateLineExtract(params);

        // then
        String name = response.jsonPath().getString("name");
        String color = response.jsonPath().getString("color");
        List<Map<String, Object>> stations = response.jsonPath().getList("stations");

        assertThat(name).isEqualTo("신분당선");
        assertThat(color).isEqualTo("bg-red-600");
        assertThat(stations).hasSize(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(0).get("name")).isEqualTo("강남역");
        assertThat(stations.get(1).get("id")).isEqualTo(2);
        assertThat(stations.get(1).get("name")).isEqualTo("판교역");
    }

    @Test
    @DisplayName("지하철 노선 목록을 조회한다.")
    void viewLineList() {
        // given
        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "신분당선");
        params1.put("color", "bg-red-600");
        params1.put("upStationId", 1);
        params1.put("downStationId", 2);
        params1.put("distance", 10);

        getCreateLineExtract(params1);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("name", "분당선");
        params2.put("color", "bg-green-600");
        params2.put("upStationId", 1);
        params2.put("downStationId", 3);
        params2.put("distance", 7);

        getCreateLineExtract(params2);

        // when
        List<Map<String, Object>> response = requestSpecificationWithLog()
                .when().get("/lines")
                .then().log().all()
                .extract().jsonPath().getList("$");

        // then
        assertThat(response).hasSize(2);

        assertThat(response.get(0).get("name")).isEqualTo("신분당선");
        assertThat(response.get(0).get("color")).isEqualTo("bg-red-600");
        List<Map<String, Object>> stations1 = (List<Map<String, Object>>) response.get(0).get("stations");
        assertThat(stations1.size()).isEqualTo(2);
        assertThat(stations1.get(0).get("id")).isEqualTo(1);
        assertThat(stations1.get(1).get("id")).isEqualTo(2);

        assertThat(response.get(1).get("name")).isEqualTo("분당선");
        assertThat(response.get(1).get("color")).isEqualTo("bg-green-600");
        List<Map<String, Object>> stations2 = (List<Map<String, Object>>) response.get(1).get("stations");
        assertThat(stations2.size()).isEqualTo(2);
        assertThat(stations2.get(0).get("id")).isEqualTo(1);
        assertThat(stations2.get(1).get("id")).isEqualTo(3);
    }

    @Test
    @DisplayName("지하철 노선을 조회한다.")
    void viewLine() {
        // given
        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "신분당선");
        params1.put("color", "bg-red-600");
        params1.put("upStationId", 1);
        params1.put("downStationId", 2);
        params1.put("distance", 10);

        long lineId = getCreateLineExtract(params1).jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> response = requestSpecificationWithLog()
                .when().get("/lines/" + lineId)
                .then().log().all()
                .extract();

        // then
        Long responseLineId = response.jsonPath().getLong("id");
        String responseLineName = response.jsonPath().getString("name");
        String responseLineColor = response.jsonPath().getString("color");
        List<Map<String, Object>> stations = response.jsonPath().getList("stations");

        assertThat(responseLineId).isEqualTo(lineId);
        assertThat(responseLineName).isEqualTo("신분당선");
        assertThat(responseLineColor).isEqualTo("bg-red-600");
        assertThat(stations).hasSize(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
    }


    @Test
    @DisplayName("지하철 노선을 수정한다.")
    void updateLine() {
        // given
        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "신분당선");
        params1.put("color", "bg-red-600");
        params1.put("upStationId", 1);
        params1.put("downStationId", 2);
        params1.put("distance", 10);

        long lineId = getCreateLineExtract(params1).jsonPath().getLong("id");

        // when
        Map<String, Object> putParams = new HashMap<>();
        putParams.put("name", "다른분당선");
        putParams.put("color", "bg-red-700");

        ExtractableResponse<Response> putResponse = requestSpecificationWithLog()
                .body(putParams)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().patch("/lines/" + lineId)
                .then().log().all()
                .extract();

        ExtractableResponse<Response> viewResponse = requestSpecificationWithLog()
                .when().get("/lines/" + lineId)
                .then().log().all()
                .extract();

        // then
        assertThat(putResponse.statusCode()).isEqualTo(200);

        assertThat(viewResponse.jsonPath().getString("name")).isEqualTo("다른분당선");
        assertThat(viewResponse.jsonPath().getString("color")).isEqualTo("bg-red-700");
    }

    @Test
    @DisplayName("지하철 노선을 삭제한다.")
    void DeleteLine() {
        // given
        Map<String, Object> params1 = new HashMap<>();
        params1.put("name", "신분당선");
        params1.put("color", "bg-red-600");
        params1.put("upStationId", 1);
        params1.put("downStationId", 2);
        params1.put("distance", 10);

        long lineId = getCreateLineExtract(params1).jsonPath().getLong("id");
        // when
        ExtractableResponse<Response> deleteResponse = requestSpecificationWithLog()
                .when().delete("/lines/" + lineId)
                .then().log().all()
                .extract();

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(204);
    }

    private ExtractableResponse<Response> getCreateLineExtract(Map<String, Object> params) {
        return requestSpecificationWithLog()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();
    }

    private RequestSpecification requestSpecificationWithLog() {
        return RestAssured.given().log().all();
    }

    private class LineAndStationSetup extends DatabaseSetupTemplate {

        public LineAndStationSetup(JdbcTemplate jdbcTemplate) {
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
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('강남역')");
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('판교역')");
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('광교역')");
        }
    }
}
