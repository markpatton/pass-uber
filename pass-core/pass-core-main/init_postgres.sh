#! /bin/sh

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE USER pass WITH PASSWORD 'moo';
	CREATE DATABASE pass;
	GRANT ALL PRIVILEGES ON DATABASE pass TO pass;
EOSQL
