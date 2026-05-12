#!/bin/bash
# swupdate-webui-auth-init.sh — I7: generate web UI credentials on first boot.
# Writes /etc/swupdate/webui.passwd in Mongoose MD5-digest htpasswd format.
# Skips if the file already exists (idempotent).

set -euo pipefail

PASSWD_FILE=/etc/swupdate/webui.passwd
REALM=acadia
USER=admin

if [[ -f "${PASSWD_FILE}" ]]; then
    exit 0
fi

# Generate a random 12-char base64 password
PASSWORD=$(openssl rand -base64 12 | tr -d '=+/' | head -c 12)

# Mongoose htpasswd uses MD5(user:realm:password)
HASH=$(printf '%s:%s:%s' "${USER}" "${REALM}" "${PASSWORD}" | md5sum | awk '{print $1}')

install -d "$(dirname "${PASSWD_FILE}")"
printf '%s:%s:%s\n' "${USER}" "${REALM}" "${HASH}" > "${PASSWD_FILE}"
chmod 0600 "${PASSWD_FILE}"

MSG="[swupdate-webui] First-boot credentials: user=${USER} password=${PASSWORD} (change after login)"
echo "${MSG}" | systemd-cat -t swupdate-webui-auth -p info || true
echo "${MSG}" > /dev/console || true
