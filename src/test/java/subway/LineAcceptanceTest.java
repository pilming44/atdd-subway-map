package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LineAcceptanceTest {

    @Test
    @DisplayName("지하철 노선을 생성한다")
    @Sql("/sql/test-data.sql")
    void createLine() {
        // given 새로운 지하철 노선 정보를 입력
        Map<String, Object> params = new HashMap<>();
        params.put("name", "신분당선");
        params.put("color", "bg-red-600");
        params.put("upStationId", 1);
        params.put("downStationId", 2);
        params.put("distance", 10);

        // when 관리자가 노선을 생성
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        // then 해당 노선이 생성되고 노선 목록에 포함
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

}
