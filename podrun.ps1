#
# Start up an Postgress Database server in podman under Windows.
#
# Note: All of the following is NOT VALID when using the per-app VPN.
#
# The image is 4.8 GB.  If download hangs or is too slow
# to complete, consider setting environment of the podman
# host as follows:
#
#     wsl --shutdown
#     $env:NO_PROXY="artifactory.ontwikkel.rotterdam.nl"
#     $env:http_proxy="http://proxy.ontwikkel.rotterdam.nl:8080/"
#     $env:https_proxy="http://proxy.ontwikkel.rotterdam.nl:8080/"
#
# and use correct IP address (some DNS servers have broken address)
#
#     172.16.140.5 artifactory.ontwikkel.rotterdam.nl
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

