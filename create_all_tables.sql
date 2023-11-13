do $$
begin
    if not exists (select from pg_type where typname = 'transport_type_') then
        create type transport_type_ as enum ('CAR', 'MOTORBIKE', 'SHIP', 'PLANE');
    end if;
    if not exists (select 1 from pg_type where typname = 'sex_') then
        create type sex_ as enum ('FEMALE', 'MALE', 'OTHER');
    end if;
    if not exists (select 1 from pg_type where typname = 'user_active_status') then
        create type user_active_status as enum ('ACTIVE', 'BLOCKED', 'DISABLED');
    end if;
    if not exists (select 1 from pg_type where typname = 'license_category') then
        create type license_category as enum ('A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'D1', 'D2');
    end if;
    if not exists (select 1 from pg_type where typname = 'grade') then
        create type grade as enum ('1', '2', '3', '4', '5');
    end if;
    if not exists (select 1 from pg_type where typname = 'trip_owner_type') then
        create type trip_owner_type as enum ('DRIVER', 'PASSENGER');
    end if;
    if not exists (select 1 from pg_type where typname = 'trip_status') then
        create type trip_status as enum
            ('DRAFT', 'WAITING_DRIVER', 'WAITING_PASSENGER', 'READY_FOR_TRIP', 'IN_PROGRESS', 'COMPLETED');
    end if;
end $$;

create table if not exists transport_make (
    id serial primary key,
    make varchar(25) not null,

    constraint make_unique
        unique (make)
);

create table if not exists transport_model (
    id serial primary key,
    make_id integer not null,
    name varchar(25) not null,

    constraint model_unique
       unique (make_id, name),
    constraint make_id_fk
       foreign key (make_id) references transport_make(id)
       on update cascade
       on delete cascade
);

create table if not exists transport_details (
    model_id integer primary key,
    type transport_type_ not null,

    constraint details_unique
        unique (model_id, type),
    constraint model_id_fk
        foreign key (model_id) references transport_model(id)
        on update cascade
        on delete cascade
);


create table if not exists transport (
    id serial primary key,
    transport_details_id integer not null,
    year smallint not null,
    country varchar(30),
    reg_number varchar(13) not null,
    existing_seats integer not null,

    constraint is_year_correct
        check ((year <= date_part('year', now())) and (year >= 1800)),
    constraint transport_details_unique
        unique (transport_details_id, year, country, reg_number, existing_seats),
    constraint transport_details_id_fk
        foreign key (transport_details_id) references transport_details(model_id)
        on update cascade
        on delete cascade
);

create table if not exists user_account (
    id serial primary key,
    first_name varchar(25) not null,
    last_name varchar(25) not null,
    birth_date date not null,
    sex sex_ not null,
    phone_number varchar(12) not null,
    email varchar(50) not null,
    active_status user_active_status not null,
    password varchar(100) not null,
    password_salt varchar,
    password_hash_algoritm varchar,

    constraint is_phone_number_correct
        check (phone_number ~ '^((8|\+7)[\- ]?)?(\(?\d{3}\)?[\- ]?)?[\d\- ]{7,}$'),
    constraint email_unique
        unique (email),
    constraint phone_number_unique
        unique (phone_number)
);

create table if not exists driver (
    id integer primary key,

    constraint id_fk
        foreign key (id) references user_account(id)
        on update cascade
        on delete cascade
);

create table if not exists drivers_license (
    id serial primary key,
    driver_id integer not null,
    category license_category not null,
    license_number varchar not null,

    constraint license
        unique (license_number),
    constraint driver_id_fk
        foreign key (driver_id) references driver(id)
        on update cascade
        on delete cascade
);

create table if not exists driver_transport (
    driver_id integer not null,
    transport_id integer not null,

    constraint transport_unique
        unique (driver_id, transport_id),
    constraint driver_id_fk
        foreign key (driver_id) references driver(id)
        on update cascade
        on delete cascade,
    constraint transport_id_fk
        foreign key (transport_id) references transport(id)
        on update cascade
        on delete cascade
);

create table if not exists rating (
    id serial primary key,
    user_account_id integer not null,
    trip_id integer not null,
    grade grade not null,

    constraint grade_unique
        unique (user_account_id, trip_id),
    constraint user_account_id_fk
        foreign key (user_account_id) references user_account(id)
        on update cascade
        on delete cascade
);

create table if not exists review (
    id serial primary key,
    user_account_id integer not null,
    trip_id integer not null,
    text text,

    constraint trip_id_unique
        unique (user_account_id, trip_id),
    constraint user_account_id_fk
        foreign key (user_account_id) references user_account(id)
        on update cascade
        on delete cascade
);

create table if not exists passenger (
    id integer primary key,

    constraint id_fk
        foreign key (id) references user_account(id)
        on update cascade
        on delete cascade
);

create table if not exists location (
    id serial primary key,
    name varchar not null,
    latitude decimal(11, 7) not null,
    longitude decimal(11, 7) not null,

    constraint location_unique
        unique (name, latitude, longitude)
);

create table if not exists trip (
    id serial primary key,
    owner_type trip_owner_type not null,
    owner_id integer not null,
    driver_id integer,
    status trip_status not null,
    date date,
    available_seats integer not null,
    departure_location_id integer not null,
    arrival_location_id integer not null,
    description text,

-- было бы славно навесить чек на места, но не знаю возможно ли это
    constraint positive_available_seats
        check (available_seats >= 0),
    constraint not_equal_locations
        check (departure_location != arrival_location),
    constraint owner_id_fk
        foreign key (owner_id) references user_account(id)
        on update cascade
        on delete cascade,
    constraint driver_id_fk
        foreign key (driver_id) references driver(id)
        on update cascade
        on delete cascade,
    constraint departure_location_fk
        foreign key (departure_location) references location(id)
        on update cascade
        on delete cascade,
    constraint arrival_location_fk
        foreign key (arrival_location) references location(id)
        on update cascade
        on delete cascade
);

create table if not exists trip_passenger (
    trip_id integer not null,
    passenger_id integer not null,

    constraint trip_id_fk
        foreign key (trip_id) references trip(id)
        on update cascade
        on delete cascade,

    constraint passenger_id_fk
        foreign key (passenger_id) references passenger(id)
        on update cascade
        on delete cascade
);
