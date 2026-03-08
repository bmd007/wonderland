create table myschema.outbox
(
    id         varchar2(255) not null,
    version    number(8)     not null,
    data       clob          not null,
    topic_name varchar2(255) not null,
    created    timestamp     not null
);

create index myschema.outbox_id_version_topic_idx on myschema.outbox (id, version, topic_name);