cd ../..

Set-Variable -Name "BASE" -Value (Get-Location)
Set-Variable -Name "VERSION" -Value "latest"

docker build --file /keycloak/Dockerfile --tag registry.dvbern.ch/vacme/keycloak:latest keycloak/