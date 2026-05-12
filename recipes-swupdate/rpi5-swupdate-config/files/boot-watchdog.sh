#!/bin/bash
# boot-watchdog.sh — C1 boot rollback watchdog
#
# Runs early on every boot (before basic.target, after local-fs.target).
# Reads /boot/boot_state and:
#   - If confirmed=true  → nothing to do, slot is healthy.
#   - If confirmed=false → decrement tries_remaining.
#     If tries_remaining reaches 0 → restore cmdline.txt.prev and reboot.
#
# /boot/boot_state format (written by post-install.sh after each OTA):
#   slot=<a|b>
#   tries_remaining=<N>
#   confirmed=<true|false>

set -euo pipefail

BOOT_STATE=/boot/boot_state

# Nothing to do if no pending update or already confirmed
if [[ ! -f "${BOOT_STATE}" ]]; then
    exit 0
fi

source "${BOOT_STATE}"          # imports slot, tries_remaining, confirmed

if [[ "${confirmed:-false}" == "true" ]]; then
    exit 0
fi

mount -o remount,rw /boot

TRIES=$(( ${tries_remaining:-0} - 1 ))

if [[ "${TRIES}" -le 0 ]]; then
    echo "[boot-watchdog] tries_remaining exhausted — rolling back" | tee /dev/console
    # Restore previous cmdline.txt
    if [[ -f /boot/cmdline.txt.prev ]]; then
        cp /boot/cmdline.txt.prev /boot/cmdline.txt
    fi
    # Restore previous kernel/DTB if backed up
    for f in kernel_2712.img bcm2712-rpi-5-b.dtb; do
        [[ -f "/boot/${f}.prev" ]] && cp "/boot/${f}.prev" "/boot/${f}" || true
    done
    rm -f "${BOOT_STATE}" /boot/cmdline.txt.prev \
          /boot/kernel_2712.img.prev /boot/bcm2712-rpi-5-b.dtb.prev
    mount -o remount,ro /boot
    reboot
else
    # Decrement counter and persist
    sed -i "s/tries_remaining=.*/tries_remaining=${TRIES}/" "${BOOT_STATE}"
    mount -o remount,ro /boot
fi
