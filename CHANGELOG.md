# Changelog

## [Unreleased]

### Added — full feature-complete OS layer (`meta/meta-joshua`)

- **Complete Yocto layer** committed locally in `meta/meta-joshua`:
  - `recipes-core/images/joshua-image.bb` + `joshua-image-common.inc` + 20
    per-domain image variants (aerospace, automotive, medical, defense, etc.)
  - `recipes-core/joshua-data-init` — first-boot persistent `/home` init
  - `recipes-core/joshua-services` — dmesg capture + disk-usage monitoring
  - `recipes-core/systemd` — persistent journal + NTP timesyncd config
  - `recipes-core/os-release` — identifies image as "Joshua OS"
  - `recipes-core/packagegroup-joshua-middleware` / `-cloud`
  - `recipes-connectivity/joshua-connectivity` — Ethernet/WiFi/Bluetooth
  - `recipes-connectivity/joshua-timesync`
  - `recipes-bsp/joshua-hal` — HAL recipe (CMake + gRPC)
  - `recipes-support/expand-rootfs` — grow root to fill SD card
  - `recipes-support/swupdate` — RSA signing config fragment
  - `recipes-swupdate/joshua-swupdate-config` — A/B OTA runtime
  - `wic/sdimage-am62x.wks` — A/B OTA partition layout
  - `swupdate/sw-description` + `pre-install.sh` + `post-install.sh`
- **`scripts/generate-swu-keys.sh`** — RSA-4096 keypair generator
- **`CLAUDE.md`** and **`REPO_MAP.md`** — project documentation

### Fixed

- `.gitignore` now tracks `meta/meta-joshua/` locally while ignoring upstream
  `layers/`; removed stray `.gitignore copy`.
- `config` now points at `Joshua-HAL` (was a leftover `Acaida-HAL` typo).

### Notes

- Joshua-OS is a direct port of the Acadia/Zion reference template, retargeted
  for TI Sitara AM62A/AM64x (machine `am62axx-evm`). Boot chain (U-Boot SPL →
  U-Boot → Linux), A/B OTA, and service layout preserved; kernel image
  (`Image`), DTB (`k3-am62a7-sk.dtb`), and U-Boot env flow adapted.
