# Joshua Yocto BSP

Containerized Yocto/OpenEmbedded build system using `kas` + Docker.

Default target is now Joshua on TI Sitara AM62A (`SK-AM62A-LP` class board using machine `am62axx-evm`).

## Quick Start

```bash
# Build default board (Joshua / TI AM62A)
make image

# Explicit board selection
make image joshua-am62a

# Open shell in build container
make shell joshua-am62a

# Flash latest WIC image
make flash joshua-am62a DEVICE=/dev/sdX
```

## Prerequisites

- Docker 20.10+
- GNU Make 4.0+
- curl
- bmaptool (for `make flash`)

## Board Support

Default board:

- `joshua-am62a` -> `am62axx-evm` (TI Sitara AM62A / SK-AM62A-LP class)

## KAS Layout

- `kas/common.yaml`: shared distro/target/cache settings
- `kas/boards/*.yaml`: machine-specific board files
- `kas/repos/*.yaml`: external layer repository definitions

Layer repos:

- `poky`
- `meta-openembedded`
- `meta-arm`
- `meta-ti`
- `meta-swupdate`
- `meta-joshua`

## Common Targets

- `make build [board]` or `make image [board]`
- `make sdk [board]`
- `make esdk [board]`
- `make shell [board]`
- `make flash [board] DEVICE=/dev/sdX`
- `make help`

## Examples

```bash
# Joshua / TI AM62A
make image joshua-am62a
make sdk joshua-am62a
```

## Troubleshooting

- Show board list and commands: `make help`
- If kas image is missing: `make kas-image`
- Rebuild from scratch: `make clean-all && make image joshua-am62a`
