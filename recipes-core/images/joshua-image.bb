SUMMARY = "Acadia OS — production image for Raspberry Pi 5"
DESCRIPTION = "Full image with A/B OTA, gRPC/Protobuf, Acadia-HAL, \
               Acadia-Client daemon, and hardware interface tooling."
HOMEPAGE = "https://github.com/SiliconCentric/Acadia-OS"
LICENSE = "MIT"

# Inherit core-image (provides IMAGE_INSTALL, IMAGE_FEATURES framework).
# Add ssh-server-openssh to match core-image-full-cmdline behaviour.
inherit core-image
IMAGE_FEATURES += "ssh-server-openssh"

# OTA / swupdate
IMAGE_INSTALL:append = " swupdate swupdate-www rpi5-swupdate-config"

# Home partition init
IMAGE_INSTALL:append = " rpi5-home-init"

# Connectivity
IMAGE_INSTALL:append = " \
    rpi5-connectivity wpa-supplicant iw rfkill bluez5 bluez5-dev \
"

# gRPC + Protobuf (NO compiler on device — M5)
IMAGE_INSTALL:append = " \
    protobuf protobuf-lite grpc \
    python3-grpcio python3-protobuf sudo \
"

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

# Acadia stack
# NOTE: acadia-client is excluded until the upstream repo exists and SRCREV is
# pinned. Add it back once available: acadia-client acadia-data-init
IMAGE_INSTALL:append = " acadia-hal acadia-services"

# Operator account — locked, SSH-key access only
inherit extrausers
EXTRA_USERS_PARAMS = "\
    useradd -m -s /bin/bash -G sudo rpi; \
    usermod -L rpi; \
"
