create table event_outbox
(
    id         UUID      not null,
    version    int4      not null,
    data       jsonb     not null,
    topic_name varchar   not null,
    created    timestamp not null
);

create
unique index id_version_topic_idx on event_outbox (id, version, topic_name);