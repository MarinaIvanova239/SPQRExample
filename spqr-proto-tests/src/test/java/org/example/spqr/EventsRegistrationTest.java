package org.example.spqr;

import org.example.spqr.models.domain.EntityState;
import org.example.spqr.models.rq.EntityIdsRq;
import org.example.spqr.models.rq.EntityRq;
import org.example.spqr.models.rq.EventRq;
import org.example.spqr.models.rs.EntityRs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WebApp.class)
@ActiveProfiles("test")
public class EventsRegistrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_eventsRegistration() {
        // given
        var entityRq1 = new EntityRq();
        entityRq1.setEntityId("ext-1");
        entityRq1.setScenarioName("scenario");
        entityRq1.setPreconditions(emptyList());
        // and
        var entityRq2 = new EntityRq();
        entityRq2.setEntityId("ext-5");
        entityRq2.setScenarioName("scenario");
        entityRq2.setPreconditions(emptyList());
        // and
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // and
        HttpEntity<EntityRq> httpRq1 = new HttpEntity<>(entityRq1, headers);
        HttpEntity<EntityRq> httpRq2 = new HttpEntity<>(entityRq2, headers);
        // and
        ResponseEntity<EntityRs> createdEntity1 = restTemplate.exchange(
                "http://localhost:" + port + "/entities",
                HttpMethod.POST,
                httpRq1,
                EntityRs.class
        );
        ResponseEntity<EntityRs> createdEntity2 = restTemplate.exchange(
                "http://localhost:" + port + "/entities",
                HttpMethod.POST,
                httpRq2,
                EntityRs.class
        );
        // and
        EntityRs created1 = createdEntity1.getBody();
        assertThat(created1.getStateId()).isEqualTo(EntityState.NEW.value());
        // and
        EntityRs created2 = createdEntity2.getBody();
        assertThat(created2.getStateId()).isEqualTo(EntityState.NEW.value());

        // when
        var suspension = new EntityIdsRq();
        suspension.setEntityIds(List.of(entityRq1.getEntityId(), entityRq2.getEntityId()));
        HttpEntity<EntityIdsRq> httpSuspension = new HttpEntity<>(suspension, headers);
        // and
        ResponseEntity<List<EntityRs>> suspendedEntities = restTemplate.exchange(
                "http://localhost:" + port + "/suspensions",
                HttpMethod.PUT,
                httpSuspension,
                new ParameterizedTypeReference<List<EntityRs>>() {}
        );
        assertThat(suspendedEntities.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        // and
        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(300))
                .pollDelay(Duration.ofMillis(100))
                .ignoreExceptions() // сеть/временные 5xx не рвут ожидание
                .untilAsserted(() -> {
                    ResponseEntity<EntityRs> entityRs1 = restTemplate.getForEntity(
                            "http://localhost:" + port + "/entities/" + entityRq1.getEntityId(),
                            EntityRs.class
                    );
                    assertThat(entityRs1.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entityRs1.getBody()).isNotNull();
                    assertThat(entityRs1.getBody().getStateId())
                            .isEqualTo(EntityState.SUSPENDED.value());

                    ResponseEntity<EntityRs> entityRs2 = restTemplate.getForEntity(
                            "http://localhost:" + port + "/entities/" + entityRq2.getEntityId(),
                            EntityRs.class
                    );
                    assertThat(entityRs2.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entityRs2.getBody()).isNotNull();
                    assertThat(entityRs2.getBody().getStateId())
                            .isEqualTo(EntityState.SUSPENDED.value());
                });

        // then
        var event1 = new EventRq();
        event1.setEventName("event1");
        var event2 = new EventRq();
        event2.setEventName("event2");
        // and
        var bulkEvents = Map.of(
                entityRq1.getEntityId(), event1,
                entityRq2.getEntityId(), event2
        );
        // and
        HttpEntity<Map<String, EventRq>> httpBulkEvent = new HttpEntity<>(bulkEvents);
        // and
        ResponseEntity<EntityRs> bulkEventsResponse = restTemplate.exchange(
                "http://localhost:" + port + "/events/bulk",
                HttpMethod.POST,
                httpBulkEvent,
                EntityRs.class
        );
        assertThat(bulkEventsResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    }

}
