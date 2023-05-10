Set-Variable -Name "BASE" -Value (Get-Location)
Set-Variable -Name "VERSION" -Value "latest"

docker build --file /frontend/docker/Dockerfile -tag registry.dvbern.ch/vacme/initalreg:latest --build-arg COPY_APP_PATH=vacme-initalreg ../../../frontend/
