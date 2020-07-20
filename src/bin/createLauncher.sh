#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "${DIR}" || exit 1

LAUNCHER=~/.local/share/applications/ride.desktop
{
echo "[Desktop Entry]
Name=Ride
Exec=${DIR}/ride.sh
Comment=Ride, an R IDE for Renjin
Terminal=false
Icon=${DIR}/ride-icon.png
Type=Application
Categories=Development"
} > ${LAUNCHER}

chmod +x ride.sh
chmod +x ${LAUNCHER}
ln -s ${LAUNCHER} ~/Desktop/ride.desktop

echo "Launcher shortcuts created!"