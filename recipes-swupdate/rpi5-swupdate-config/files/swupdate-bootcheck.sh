#!/bin/bash
# swupdate-bootcheck.sh — I4/C1: health check that confirms OTA or triggers rollback.
#
# Runs after the normal boot target is up.
# If the system reaches multi-user.target → write confirmed=true to /boot/boot_state.
# If not healthy              → restore previous slot from .prev backups and reboot.

set -euo pipefail

BOOT_STATE=/boot/boot_state

# If there is no boot_state the system was not recently updated — nothing to do.
if [[ ! -f "${BOOT_STATE}" ]]; then
    exit 0
fi

source "${BOOT_STATE}"    # imports slot, tries_remaining, confirmed

# Already confirmed by a previous run
if [[ "${confirmed:-false}" == "true" ]]; then
    exit 0
fi

# ── Health check ─────────────────────────────────────────────────────────────
HEALTH_OK=true
if ! systemctl is-system-running --wait 2>/dev/null | grep -Eq 'running|degraded'; then
    HEALTH_OK=false
fi

if [[ "${HEALTH_OK}" == "true" ]]; then
    mount -o remount,rw /boot
    sed -i 's/confirmed=false/confirmed=true/' "${BOOT_STATE}"
    mount -o remount,ro /boot
    logger -t swupdate-bootcheck "Slot ${slot} confirmed healthy"
else
    logger -t swupdate-bootcheck "Slot ${slot} unhealthy — rolling back"
    mount -o remount,rw /boot
    if [[ -f /boot/cmdline.txt.prev ]]; then
        cp /boot/cmdline.txt.prev /boot/cmdline.txt
    fi
    for f in kernel_2712.img bcm2712-rpi-5-b.dtb; do
        [[ -f "/boot/${f}.prev" ]] && cp "/boot/${f}.prev" "/boot/${f}" || true
    done
    rm -f "${BOOT_STATE}" /boot/cmdline.txt.prev \
          /boot/kernel_2712.img.prev /boot/bcm2712-rpi-5-b.dtb.prev
    mount -o remount,ro /boot
    reboot
fi
