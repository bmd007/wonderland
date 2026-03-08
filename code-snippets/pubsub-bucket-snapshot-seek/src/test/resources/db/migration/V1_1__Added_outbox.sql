create table outbox
(
    id         varchar   not null,
    version    int8      not null,
    data       varchar   not null,
    topic_name varchar   not null,
    created    timestamp not null
);

create
unique index outbox_id_version_topic_idx on outbox (id, version, topic_name);