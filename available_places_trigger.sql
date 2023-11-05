create or replace function check_available_places_in_trip()
    returns trigger as $check_available_places$
    begin
        if new.available_places = 0 then
            new.status = 'ready_for_trip';
        end if;
    end;
$check_available_places$ language plpgsql;

drop trigger if exists check_available_places on trip;
create trigger check_available_places
    after update of available_places on trip
    for each statement
    execute function check_available_places_in_trip();