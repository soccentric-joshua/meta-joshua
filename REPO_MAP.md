# Joshua-OS

**Remote:** `git@github.com:SiliconCentric/Joshua-OS.git`
**Parent project:** [Joshua](../Joshua.md) (target hardware: **TI Sitara AM62x/AM64x** SoC + PRU-ICSS south-bridge.)
**Leaf type:** `<Project>-OS`
**Status:** REAL — actively built out

Yocto-based embedded Linux OS for the **TI Sitara AM62A/AM64x** platform.
Built with `kas` + Docker against the TI `scarthgap` layer set, with A/B OTA
updates via `swupdate`.

**Board:** default `joshua-am62a` (machine `am62axx-evm`, SK-AM62A-LP class).
Platform customization lives in `meta/meta-joshua` (committed locally);
upstream layers (`poky`, `meta-openembedded`, `meta-arm`, `meta-ti`,
`meta-swupdate`) are checked out by kas into a **gitignored** `layers/`
directory and must never be hand-edited or tracked.

**Targets:** `make build`, `make image`, `make shell [board]`, `make swu`,
`make sdk`/`esdk`, `make flash`, `make deploy-*`, `make clean`/`clean-all`.

**Boot flow (AM62A):**
```
SD/eMMC → ROM → U-Boot SPL (tispl.bin) → U-Boot (u-boot.img)
        → Linux (Image + k3-am62a7-sk.dtb) → systemd → Joshua-HAL, services
```

**A/B OTA layout:** `boot` (FAT32), `rootfs-a`, `rootfs-b` (ext4, switched by
swupdate), `data` (`/home`, persistent).

**Local layer:** `meta/meta-joshua` is committed in-repo (see `.gitignore`),
matching the Acadia/Arches/Zion convention where the product layer is tracked
locally while upstream layers are fetched by kas.

---
Part of the `Joshua` project in the `project-managers` fleet. See
`docs/project-managers.md` for the full 9-project / 8-leaf hierarchy and shared
conventions.
