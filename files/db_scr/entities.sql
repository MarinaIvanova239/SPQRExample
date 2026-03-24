CREATE TABLE ENTITIES
(
    ENTITY_ID               VARCHAR(128) NOT NULL,
    CREATED_AT              TIMESTAMP(6) NOT NULL,
    ACTIVATED_AT            TIMESTAMP(6),
    STATE_ID                SMALLINT     NOT NULL,
    UPDATED_AT              TIMESTAMP(6),
    SCENARIO_ID             VARCHAR(512)
);