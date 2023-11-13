drop trigger if exists check_available_seats on trip cascade;

create or replace function check_available_seats_in_trip()
    returns trigger language plpgsql as $check_available_seats$
    begin
        if new.available_seats = 0 then
			update trip set status = 'READY_FOR_TRIP' where id = new.id;
        end if;
	    return new;
    end; $check_available_seats$;

create trigger check_available_seats
    after update of available_seats on trip
    for each row
    execute function check_available_seats_in_trip();


-- !!!FOR CHECKING!!!
-- select * from trip;
-- insert into user_account (id, first_name, last_name, birth_date, sex,
--     phone_number, email, active_status, password)
-- values 
-- 	(1,'abc', 'abc', '11-11-2011', 'male', '89111111111', 'abc', 'active', 'abc');
--insert into driver (id) values (1);
-- insert into location (name, latitude, longitude) values ('1', 10.1, 10.2);
-- insert into location (name, latitude, longitude) values ('2', 10.3, 10.4);
-- insert into trip (owner_type, owner_id, driver_id, status, 
-- 				  available_seats, departure_location, arrival_location)
-- values 
-- 	('driver',  1, 1, 'waiting_passenger', 1, 1, 2);