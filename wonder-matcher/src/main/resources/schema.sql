create table if not exists wonder_seekers (
    id                     varchar                  not null,
    last_location          geometry                 not null default 'point(0 0)',
    updated_at     timestamp with time zone         not null default timestamp '2000-01-01',
    primary key (id)
);

create spatial index if not exists WONDER_SEEKERS_spatial_index on WONDER_SEEKERS(last_location);
