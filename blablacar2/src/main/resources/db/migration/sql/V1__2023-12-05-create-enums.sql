CREATE SCHEMA public;
do
$$
    begin
        if
            not exists(select from pg_type where typname = 'transport_type_') then
            create type transport_type_ as enum ('CAR', 'MOTORBIKE', 'SHIP', 'PLANE');
        end if;
        if
            not exists(select 1 from pg_type where typname = 'sex_') then
            create type sex_ as enum ('FEMALE', 'MALE', 'OTHER');
        end if;
        if
            not exists(select 1 from pg_type where typname = 'user_active_status') then
            create type user_active_status as enum ('ACTIVE', 'BLOCKED', 'DISABLED');
        end if;
        if
            not exists(select 1 from pg_type where typname = 'license_category') then
            create type license_category as enum ('A1', 'A2', 'B1', 'B2', 'C1', 'C2', 'D1', 'D2');
        end if;
        if
            not exists(select 1 from pg_type where typname = 'grade') then
            create type grade as enum ('1', '2', '3', '4', '5');
        end if;
        if
            not exists(select 1 from pg_type where typname = 'trip_owner_type') then
            create type trip_owner_type as enum ('DRIVER', 'PASSENGER');
        end if;
        if
            not exists(select 1 from pg_type where typname = 'trip_status') then
            create type trip_status as enum ('DRAFT', 'WAITING_DRIVER', 'WAITING_PASSENGER', 'READY_FOR_TRIP', 'IN_PROGRESS', 'COMPLETED');
        end if;
    end
$$;
