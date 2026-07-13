#!/bin/bash
# swupdate-start.sh — wrapper that conditionally enables RSA signing and web UI auth.
# Called by swupdate.service.d/webui.conf drop-in instead of /usr/bin/swupdate directly.

set -euo pipefail

CFG=/etc/swupdate/swupdate.cfg
KEY=/etc/swupdate/swupdate-public.pem
PASSWD=/etc/swupdate/webui.passwd

KEY_ARGS=""
AUTH_ARGS=""
SECURE_MODE=false

if [[ -f /etc/swupdate/secure-mode ]]; then
    SECURE_MODE=true
fi

# RSA signed-image verification (C2)
if [[ -f "${KEY}" ]]; then
    KEY_ARGS="-k ${KEY}"
elif [[ "${SECURE_MODE}" == "true" ]]; then
    echo 'Secure SWUpdate mode requires /etc/swupdate/swupdate-public.pem' >&2
    exit 1
fi

# Mongoose web UI authentication (I7)
if [[ -f "${PASSWD}" ]]; then
    AUTH_ARGS=" --auth_domain=acadia --htpasswd_file=${PASSWD}"
fi

exec /usr/bin/swupdate \
    -f "${CFG}" \
    ${KEY_ARGS} \
    -w "--document-root /usr/share/swupdate/www --port 8080${AUTH_ARGS}"
