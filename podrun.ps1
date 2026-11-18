#
# Start up an Postgress Database server and a mailpit server in podman under Windows.
#
#podman machine start
podman run -d `
    --tls-verify=false `
    -p 127.0.0.1:5433:5432 `
    --name verbonden-database `
    -e POSTGRES_DB=verbonden `
    -e POSTGRES_USER=verbonden_owner `
    -e POSTGRES_PASSWORD=verbonden_owner `
    -v pg2_data:/var/lib/postgresql `
    -v ./docker/postgres:/docker-entrypoint-initdb.d `
    "postgres:18"

podman run -d `
    --tls-verify=false `
    -p 127.0.0.1:1025:1025 `
    -p 127.0.0.1:8025:8025 `
    --name verbonden-mailpit `
    "axllent/mailpit:latest"

