create table if not exists wonderSeekers
(
    id                     varchar                  not null,
    last_location          geometry                 not null default 'point(0 0)',
    updated_at timestamp with time zone not null default timestamp '2000-01-01',
    primary key (id)
);

create spatial index if not exists wonderSeeker_spatial_index on wonderSeekers (last_location);
