package subway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

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

        // when

        // then
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

        // when

        // then
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

        // when

        // then
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

        // when

        // then
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

        // when

        // then
    }

    /**
     * Given 노선이 등록돼있고
     * When 관리자가 노선의 마지막 구간이 아닌 다른 구간을 삭제하면
     * Then 에러가 발생하고 해당 구간은 삭제되지않는다.
     */
    @Test
    @DisplayName("노선의 마지막 구간이 아닌 다른 구간 삭제 시 예외 발생")
    void 구간삭제_case3() {
        // given

        // when

        // then
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

        // when

        // then
    }


    private class SectionTestSetup extends DatabaseSetupTemplate {

        public SectionTestSetup(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        @Override
        protected void truncateTables() {
        }

        @Override
        protected void resetAutoIncrement() {
        }

        @Override
        protected void insertInitialData() {
        }
    }
}
