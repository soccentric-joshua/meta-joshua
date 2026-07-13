# Project Layout

Joshua OS keeps source, build state, and documentation separate:

- `layers/`: pinned external Yocto/OpenEmbedded layers.
- `recipes-*`, `conf/`, and `wic/`: the project Yocto layer and image metadata.
- `kas/`: target KAS configurations.
- `scripts/`: executable project helpers, including `kas-container`.
- `docker/`: Dockerfiles used to build KAS, SDK, and eSDK images.
- `docs/`: feature requirements and operational documentation.
- `build/`: generated BitBake state, shared downloads, and shared sstate cache.
- `deploy/`: generated images, SDK installers, and OTA bundles.
- `swupdate/`: source files used to assemble and install OTA artifacts.

Build state and deployment artifacts are ignored by Git. Use `make clean-build` or
`make clean-all` to remove generated output.
