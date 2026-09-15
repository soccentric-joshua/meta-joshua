SUMMARY = "Joshua OS — production image for TI Sitara AM62A/AM64x"
DESCRIPTION = "Full image with A/B OTA, gRPC/Protobuf, Joshua-HAL, \
               hardware interface tooling, and connectivity."
HOMEPAGE = "https://github.com/SiliconCentric/Joshua-OS"
LICENSE = "MIT"

# Base on core-image-full-cmdline (provides bash, sshd-openssh,
# package-management, base IMAGE_INSTALL framework). This is the canonical
# base for all Joshua image variants.
require recipes-extended/images/core-image-full-cmdline.bb
IMAGE_FEATURES += "ssh-server-openssh"

# OTA / swupdate
# joshua-swupdate-config implements the inactive-slot, boot-selection, and
# rollback flow for sdimage-am62x.wks.
IMAGE_INSTALL:append:am62axx-evm = " swupdate swupdate-www joshua-swupdate-config"

# Home partition init (persistent /home on the data partition)
IMAGE_INSTALL:append = " joshua-data-init"

# Root filesystem expansion (single-partition deployments)
IMAGE_INSTALL:append = " expand-rootfs"

# Connectivity
IMAGE_INSTALL:append = " \
    joshua-connectivity iw rfkill bluez5-dev \
"

# gRPC + Protobuf via packagegroup-joshua-middleware
IMAGE_INSTALL:append = " packagegroup-joshua-middleware sudo"

# Cloud/telemetry package group (not installed by default; add via
# packagegroup-joshua-cloud when deployments need MQTT/NTP/cloud connectivity)
# IMAGE_INSTALL:append = " packagegroup-joshua-cloud"

# Hardware interface tools
IMAGE_INSTALL:append = " \
    i2c-tools python3-smbus python3-smbus2 \
    spitools python3-spidev \
    libgpiod libgpiod-tools python3-gpiod \
    picocom minicom python3-pyserial \
    usbutils pciutils nvme-cli sg3-utils hdparm mtd-utils \
    can-utils \
    lshw ethtool devmem2 cpupower watchdog sysstat iotop strace \
    v4l-utils evtest python3-periphery vim \
"

# Joshua stack — HAL, validation client, services. WebConfig (the
# configuration daemon) ships via the meta-webconfig layer instead
# (kas/repos/meta-webconfig.yaml + the IMAGE_INSTALL in kas/common.yaml) —
# see the removed recipes-app/joshua-configuration/, which duplicated it
# under the package name "joshua-configuration".
IMAGE_INSTALL:append = " joshua-hal joshua-client joshua-services"

# Instrumentation (Joshua-Instrumentation's yocto layer,
# kas/repos/joshua-instrumentation.yaml). am62axx-evm has no descriptor set
# of its own; INSTR_BOARD maps it to the existing "sitara-am62" set (same
# Sitara AM62 SoC family) rather than leaving it unmapped.
IMAGE_INSTALL:append = " instrumentation"

# Target debugging and profiling
IMAGE_FEATURES += "tools-debug"
IMAGE_INSTALL:append = " gdbserver perf trace-cmd valgrind"

# Ptest package and runner support
IMAGE_FEATURES += "ptest-pkgs"

# Operator account — locked, SSH-key access only
inherit extrausers
EXTRA_USERS_PARAMS = "\
    useradd -M -s /bin/bash -G sudo joshua; \
    usermod -L joshua; \
"
