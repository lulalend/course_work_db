do $$
    declare type_names text;
    begin
        type_names := string_agg('"' || ns.nspname || '.' || t.typname || '"', ', ')
            from pg_type as t
            join
    		    pg_namespace as ns
    	on
        	(t.typnamespace = ns.oid)
    	where
            t.typcategory = 'e' and ns.nspname = 's335093';

        execute 'drop type ' || type_names;
    end; $$