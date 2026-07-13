Generic Embedded Linux Feature List
===================================

1. Project and Build Platform
-----------------------------
- Containerized Yocto/OpenEmbedded build system for embedded Linux platforms.
- All Yocto, BitBake, and kas operations run inside Docker containers.
- No Yocto, BitBake, or Python build dependencies are required on the host.
- kas release is pinned and managed automatically through the kas-container helper.
- Shared downloads and sstate-cache directories speed up subsequent builds.
- Automatic creation of the build, downloads, sstate-cache, and deploy directories.
- Target-platform selection through a positional make argument, for example:
  make image <target-platform>
- Configurable build paths, container image names, image target, SDK paths, and OTA version.
- Automatic derivation and validation of the Yocto MACHINE from the target configuration.
- Interactive shell inside the KAS/BitBake build container.

2. Target Platform Support
--------------------------
- Configuration-driven support for multiple embedded hardware platforms.
- Separate kas configuration for each target platform and machine.
- Support for removable-media, eMMC, NVMe, network-boot, and other platform-specific layouts when defined by the target configuration.
- Additional platforms can be added with a new kas board or machine configuration.
- Platform-specific bootloader, kernel, device-tree, partition, and deployment settings.

3. Image Build and Storage
--------------------------
- Custom Yocto image based on a standard base image recipe.
- Builds compressed WIC images, WIC block maps, and ext4 root filesystem images.
- Flashable image generation for supported target platforms.
- bmaptool-based flashing with block-device and image validation.
- Integration with platform-specific BSP and kernel layers.
- Configurable product or platform kernel boot arguments.
- Configurable kernel fragments for deployment-specific features.
- Optional A/B partition layout with boot, rootfs-a, rootfs-b, and persistent-data partitions.
- Persistent data partition that survives image updates when enabled by the target layout.
- First-boot expansion of persistent data partitions when supported.
- Optional root filesystem expansion service for single-partition deployments.
- Platform boot firmware and storage targets are controlled by the selected machine configuration.

4. Operating System and Connectivity
-------------------------------------
- systemd is used as the init manager.
- DHCP Ethernet configuration through systemd-networkd.
- Wi-Fi support through wpa_supplicant for compatible wireless adapters.
- Bluetooth support through BlueZ, rfkill, and wireless tools where hardware is available.
- Runtime Wi-Fi credentials can be configured in wpa_supplicant configuration.
- Configurable default user account with sudo access.
- User home directory can be placed on persistent storage.
- First-boot service can create the user home directory from /etc/skel.
- Serial-console support for platform debugging where a UART is available.

5. A/B OTA Updates
------------------
- SWUpdate integration for over-the-air system updates.
- Generation of .swu OTA bundles from the built root filesystem.
- Optional inclusion of the Linux kernel and device tree blob in OTA bundles.
- Mongoose-powered SWUpdate web interface on a configurable service port.
- Command-line delivery support through swupdate-client.
- A/B update logic writes the inactive root filesystem slot.
- Automatic active-slot detection before installation.
- Boot slot switching through the platform bootloader configuration.
- Partition- or filesystem-identifier-based root filesystem selection.
- Boot configuration write protection can be restored after updates.
- Post-reboot boot-check service verifies the selected slot and records the active slot.
- Recovery of the previous boot configuration when the requested slot was not booted.
- Timestamped or explicitly assigned OTA package versions.
- Optional RSA-4096/SHA-256 signing of OTA metadata.
- Secure production mode for signed OTA artifacts.
- Secure mode requires configured signing keys and rejects unsigned bundles.

6. Development Middleware and APIs
----------------------------------
- gRPC C++ runtime and development support.
- Protobuf runtime, compiler, lite runtime, and development support.
- Python gRPC bindings.
- Python Protobuf bindings.
- Middleware package group support.
- Cloud and telemetry package group support.
- Target debugging and profiling support.
- Ptest package and runner support.
- SDK development package support.

7. Hardware Interface and Diagnostic Tools
------------------------------------------
- I2C support through i2c-tools and Python SMBus libraries.
- SPI support through SPI tools and Python spidev libraries.
- GPIO support through libgpiod, GPIO utilities, and Python bindings.
- UART and serial tools including picocom, minicom, and Python serial support.
- USB inspection through usbutils.
- PCIe, NVMe, flash, and storage diagnostic tools.
- CAN bus tools through can-utils.
- System inspection tools including lshw, ethtool, devmem2, cpupower, sysstat, strace, v4l-utils, and evtest.
- Hardware tools are included when supported by the selected target platform.

8. SDK and Cross-Compilation
----------------------------
- Standard Yocto SDK generation with make sdk.
- Extensible Yocto SDK generation with make esdk.
- Cross-compilation toolchain installers deployed with target and timestamp directories.
- Development headers, source, and debug symbols available in SDK output.
- gdbserver, perf, trace-cmd, valgrind, and ptest support in SDK workflows.
- Standard SDK Docker image generation through make sdk-image.
- Extensible SDK Docker image generation through make esdk-image.
- Automatic staging of SDK installers into the SDK Docker build contexts.
- Conditional ensure-sdk-image and ensure-esdk-image targets to avoid unnecessary rebuilds.
- eSDK support for devtool-based recipe and source development.

9. Docker Image Management
--------------------------
- KAS Yocto build container image generation through make kas-image.
- Configurable KAS, SDK, and eSDK Docker image names and tags.
- Automatic build of missing container images through ensure-image targets.
- Docker image cleanup through make clean-docker.
- Reproducible Docker build inputs for the Yocto, SDK, and eSDK images.

10. Artifact Deployment
-----------------------
- Automatic deployment of WIC image and bmap artifacts.
- Automatic deployment of standard SDK installers.
- Automatic deployment of extensible SDK installers.
- Automatic deployment of SWUpdate bundles.
- Target- and timestamp-specific deployment directories for images, SDKs, eSDKs, and OTA packages.
- Deployment targets can be run independently after artifacts are built.

11. Customization and Extensibility
-----------------------------------
- Project-specific Yocto layer for all product customizations.
- Automatic discovery of recipes under recipes-* directories.
- Support for adding packages through the image recipe.
- Support for adding new BitBake recipes.
- Support for bbappend-based kernel and platform customization.
- Support for adding new target platforms through kas configuration files.
- Shared kas configuration for repositories, layers, machine settings, and local.conf.
- External layers are pinned to the configured Yocto release and managed by kas.
- Configurable license flags for platform components that require them.

12. Quality, Validation, and Utilities
--------------------------------------
- Makefile syntax validation through make lint.
- YAML validation for common, target, and repository configuration files.
- Prerequisite checks for Docker and curl through make test.
- Automatic validation of every target configuration and its MACHINE field.
- BitBake metadata validation through make validate.
- Validation of middleware, cloud, debug, SDK, development-package, and ptest features.
- Target configuration validation through make check-board.
- Bash tab-completion for Makefile targets and target-platform names.
- Help output listing build, Docker, deployment, clean, utility, and target options.
- Rebuild workflow that clears the selected image recipe sstate before rebuilding.
- Clean workflow for build output, Docker images, downloads, sstate-cache, and deploy artifacts.

13. Main Make Targets
---------------------
- build / image: Build and deploy the Yocto image.
- rebuild-image: Clean image recipe state and rebuild the image.
- sdk: Build and deploy the standard SDK installer.
- esdk: Build and deploy the extensible SDK installer.
- shell: Open an interactive KAS build shell.
- swu: Build and deploy an OTA update bundle.
- flash: Flash the latest image to a validated block device.
- kas-image: Build the Yocto/KAS Docker image.
- sdk-image: Build the SDK and its Docker image, then deploy artifacts.
- esdk-image: Build the eSDK and its Docker image, then deploy artifacts.
- deploy-images, deploy-sdk, deploy-esdk, deploy-swu: Deploy artifacts manually.
- completion: Install Bash tab-completion.
- lint, test, validate, check-board: Run project checks.
- clean, clean-build, clean-docker, clean-all: Remove generated outputs.
- help: Display command usage and available target platforms.
