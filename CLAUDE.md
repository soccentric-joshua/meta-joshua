# Joshua-OS — Yocto embedded Linux for TI Sitara AM62x/AM64x

## Overview

Joshua-OS is the Yocto/OpenEmbedded build system for the **Joshua** platform —
the **TI Sitara AM62A/AM64x** SoC + PRU-ICSS south-bridge. Default board:
**SK-AM62A-LP** (machine `am62axx-evm`). Built with
[kas](https://kas.readthedocs.io/) + Docker against the TI `scarthgap` layer
set, with A/B OTA updates via [swupdate](https://sbabic.github.io/swupdate/).

## Quick Start

```bash
make kas-image          # Build the KAS Docker image (once)
make image joshua-am62a # Build the Yocto image for AM62A (default board)
```

## Build Targets

```
make image  joshua-am62a     Build Yocto SD card image
make build  joshua-am62a     Alias for image
make rebuild-image joshua-am62a   cleansstate image recipe, then rebuild
make sdk    joshua-am62a     Cross-toolchain SDK installer → deploy/sdk/
make esdk   joshua-am62a     Extensible SDK installer     → deploy/esdk/
make shell  joshua-am62a     Interactive kas shell
make swu                      Build .swu OTA package
make flash  joshua-am62a DEVICE=/dev/sdX   Flash image to SD card
```

## Boot Flow (AM62A)

```
SD/eMMC → ROM → U-Boot SPL (tispl.bin) → U-Boot (u-boot.img)
        → Linux (Image + k3-am62a7-sk.dtb) → systemd → Joshua-HAL, services
```

## A/B OTA Layout

Partition table (`meta/meta-joshua/wic/sdimage-am62x.wks`):
```
  boot       FAT32   tiboot3.bin, tispl.bin, u-boot.img, Image, DTB, extlinux
  rootfs-a   ext4    slot A (active after initial flash)
  rootfs-b   ext4    slot B (updated by swupdate, then activated)
  data       ext4    /home — persistent, never touched by OTA
```

`make swu` builds an `.swu` package. Signing via
`scripts/generate-swu-keys.sh` (RSA-4096).

## Layer Structure

| Layer               | Source                        | Purpose                              |
|---------------------|-------------------------------|--------------------------------------|
| `poky`              | poky (scarthgap)              | OE core                              |
| `meta-oe`           | meta-openembedded (scarthgap) | Package set                          |
| `meta-arm`          | meta-arm (scarthgap)          | ARM BSP / ATF                       |
| `meta-ti`           | meta-ti (scarthgap)           | TI Sitara BSP, kernel, U-Boot       |
| `meta-swupdate`     | sbabic/meta-swupdate (scarthgap) | swupdate OTA framework           |
| `meta-joshua`       | SiliconCentric/meta-joshua (local, in `meta/`) | Joshua image, HAL, swu config |

## Repo Notes

- `build/`, `downloads/`, `sstate-cache/`, `layers/` are gitignored
  bind-mounted host dirs.
- `meta/meta-joshua` is committed **locally** in this repo (see `.gitignore`);
  upstream layers are cloned by kas into `layers/` and must never be
  hand-edited.
- `config` holds the Joshua-HAL repository URL.
