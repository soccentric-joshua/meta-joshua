#!/bin/bash
# bootcount-reset.sh — T051: U-Boot env OTA rollback confirmation
#
# Runs once after a successful boot as a systemd oneshot service.
# Sets upgrade_available=0 and bootcount=0 in the U-Boot environment to
# signal that the current slot is healthy and rollback is not needed.
#
# The U-Boot bootlimit / upgrade_available mechanism works as follows:
#   1. swupdate post-install sets:   upgrade_available=1  bootcount=0  bootlimit=3
#   2. On every boot, U-Boot increments bootcount.
#      If bootcount >= bootlimit and upgrade_available==1 → U-Boot reverts to
#      the previous rootfs (via altbootcmd) and clears upgrade_available.
#   3. This script runs on a healthy boot and calls fw_setenv to clear
#      upgrade_available so the counter stops accumulating.

set -euo pipefail

FW_SETENV=/usr/sbin/fw_setenv
FW_PRINTENV=/usr/sbin/fw_printenv

# Do nothing if fw_setenv is not present (factory images, non-OTA builds)
if [[ ! -x "${FW_SETENV}" ]]; then
    logger -t bootcount-reset "fw_setenv not found — skipping"
    exit 0
fi

# Read current value; if upgrade_available is already 0 nothing to do
CURRENT=$(${FW_PRINTENV} -n upgrade_available 2>/dev/null || echo "0")
if [[ "${CURRENT}" == "0" ]]; then
    logger -t bootcount-reset "upgrade_available already 0 — nothing to do"
    exit 0
fi

# Confirm the update: clear upgrade_available and reset bootcount
${FW_SETENV} upgrade_available 0
${FW_SETENV} bootcount 0

logger -t bootcount-reset \
    "Upgrade confirmed — upgrade_available cleared, bootcount reset to 0"
