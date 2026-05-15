#!/bin/bash
# SWUpdate post-install script — included in every .swu package
#
# Updates /boot/cmdline.txt so that the RPi firmware boots the newly
# written slot on the next reboot, writes boot_state for the watchdog,
# and backs up cmdline.txt for rollback recovery.

set -euo pipefail

TARGET_SLOT=$(cat /run/swupdate-target-slot)
TARGET_DEV=$(cat  /run/swupdate-target-dev)

echo "[swupdate post-install] activating slot ${TARGET_SLOT} (${TARGET_DEV})"

# Resolve the PARTUUID of the target partition
TARGET_PARTUUID=$(blkid -s PARTUUID -o value "${TARGET_DEV}")
if [[ -z "${TARGET_PARTUUID}" ]]; then
    echo "[swupdate post-install] ERROR: cannot read PARTUUID from ${TARGET_DEV}" >&2
    exit 1
fi

mount -o remount,rw /boot

# Backup current cmdline.txt for rollback (boot-watchdog / swupdate-bootcheck)
cp /boot/cmdline.txt /boot/cmdline.txt.prev

# Switch root= to the new slot
sed -i "s|root=PARTUUID=[a-fA-F0-9-]*|root=PARTUUID=${TARGET_PARTUUID}|" \
    /boot/cmdline.txt

# Write boot_state for the watchdog — 3 tries before automatic rollback
printf 'slot=%s\ntries_remaining=3\nconfirmed=false\n' "${TARGET_SLOT}" \
    > /boot/boot_state

mount -o remount,ro /boot

echo "[swupdate post-install] cmdline.txt → root=PARTUUID=${TARGET_PARTUUID}"
echo "[swupdate post-install] boot_state written (slot=${TARGET_SLOT} tries=3)"

# T051: Also set U-Boot environment variables for firmware-level rollback.
# U-Boot increments bootcount on every boot; if bootcount >= bootlimit and
# upgrade_available==1, altbootcmd reverts the slot.  The bootcount-reset
# systemd service clears upgrade_available on a healthy boot.
if command -v fw_setenv &>/dev/null; then
    fw_setenv upgrade_available 1
    fw_setenv bootcount 0
    fw_setenv bootlimit 3
    echo "[swupdate post-install] U-Boot env: upgrade_available=1 bootcount=0 bootlimit=3"
else
    echo "[swupdate post-install] fw_setenv not available — skipping U-Boot env"
fi

# Clean up temp files and symlink
rm -f /dev/swupdate-inactive \
      /run/swupdate-target-slot \
      /run/swupdate-target-dev
