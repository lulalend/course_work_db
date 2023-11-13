do $$
	declare function_names text;
begin    
    function_names := 
    string_agg('"' || ns.nspname || '"' || '.' || proname || '(' || oidvectortypes(proargtypes)|| ')', ', ' ) 
        from 
			pg_proc
    	inner join 
    		pg_namespace as ns
    	on 
        	(pg_proc.pronamespace = ns.oid)
    	where
            ns.nspname = 'public';
    
    execute 'drop function ' || function_names;
end $$;