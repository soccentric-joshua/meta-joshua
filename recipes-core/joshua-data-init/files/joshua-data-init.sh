#!/bin/bash
# joshua-data-init — runs once per boot until /home is populated.

set -euo pipefail

FLAG=/home/.home-initialised

if [[ -f "${FLAG}" ]]; then
    logger -t joshua-data-init "Home partition already initialised — skipping."
    exit 0
fi

logger -t joshua-data-init "First boot: initialising /home partition."

HOME_DEV=$(findmnt -n -o SOURCE /home)
DISK=$(lsblk -ndo PKNAME "/dev/${HOME_DEV##*/}" 2>/dev/null || echo "")
if [[ -z "${DISK}" ]]; then
    DISK=$(echo "${HOME_DEV}" | sed 's/p\?[0-9]\+$//')
fi
PART_NUM=$(cat /sys/class/block/${HOME_DEV##*/}/partition 2>/dev/null || \
           echo "${HOME_DEV}" | grep -o '[0-9]*$')

logger -t joshua-data-init "Expanding ${HOME_DEV} (disk=${DISK} part=${PART_NUM}) to 100%"
parted -s "${DISK}" resizepart "${PART_NUM}" 100%
resize2fs "${HOME_DEV}"
logger -t joshua-data-init "Expansion complete."

while IFS=: read -r username _ uid _ _ homedir shell; do
    [[ "${uid}" -lt 1000 ]] && continue
    [[ "${homedir}" != /home/* ]] && continue
    [[ "${shell}" == */nologin ]] || [[ "${shell}" == */false ]] && continue

    if [[ ! -d "${homedir}" ]]; then
        logger -t joshua-data-init "Creating ${homedir} for ${username} (uid=${uid})"
        mkdir -m 0750 -p "${homedir}"
        chown "${username}:${username}" "${homedir}"
        cp -a /etc/skel/. "${homedir}/" 2>/dev/null || true
        chown -R "${username}:${username}" "${homedir}"
    fi
done < /etc/passwd

touch "${FLAG}"
logger -t joshua-data-init "Home partition initialisation complete."
