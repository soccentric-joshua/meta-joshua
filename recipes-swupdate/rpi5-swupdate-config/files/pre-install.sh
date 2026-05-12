#!/bin/bash
# SWUpdate pre-install script — included in every .swu package
#
# Identifies the inactive rootfs slot and creates the /dev/swupdate-inactive
# symlink. Also backs up kernel and DTB for rollback recovery.

set -euo pipefail

# Find the currently active root device
ACTIVE_DEV=$(findmnt -n -o SOURCE /)
echo "[swupdate pre-install] active root: ${ACTIVE_DEV}"

case "${ACTIVE_DEV}" in
    /dev/mmcblk0p2)
        INACTIVE_DEV=/dev/mmcblk0p3
        INACTIVE_SLOT=b
        ;;
    /dev/mmcblk0p3)
        INACTIVE_DEV=/dev/mmcblk0p2
        INACTIVE_SLOT=a
        ;;
    *)
        echo "[swupdate pre-install] ERROR: unexpected active device '${ACTIVE_DEV}'" >&2
        exit 1
        ;;
esac

echo "[swupdate pre-install] target slot: ${INACTIVE_SLOT} (${INACTIVE_DEV})"

# Ensure the inactive partition is not mounted
if mountpoint -q /mnt/rootfs-inactive 2>/dev/null; then
    umount /mnt/rootfs-inactive
fi

# Backup kernel and DTB so boot-watchdog / bootcheck can restore them on rollback
mount -o remount,rw /boot
for f in kernel_2712.img bcm2712-rpi-5-b.dtb; do
    if [[ -f "/boot/${f}" ]]; then
        cp "/boot/${f}" "/boot/${f}.prev"
        echo "[swupdate pre-install] backed up /boot/${f}"
    fi
done
mount -o remount,ro /boot

# Create the stable symlink the image handler will write to
ln -sf "${INACTIVE_DEV}" /dev/swupdate-inactive

# Persist slot info for post-install
echo "${INACTIVE_SLOT}" > /run/swupdate-target-slot
echo "${INACTIVE_DEV}"  > /run/swupdate-target-dev
