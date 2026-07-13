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
IMAGE_INSTALL:append = " swupdate swupdate-client swupdate-www"
IMAGE_INSTALL:append:raspberrypi5 = " rpi5-swupdate-config"

# Home partition init
IMAGE_INSTALL:append:raspberrypi5 = " rpi5-home-init"

# Connectivity
IMAGE_INSTALL:append:raspberrypi5 = " \
    rpi5-connectivity wpa-supplicant iw rfkill bluez5 bluez5-dev \
"

# gRPC + Protobuf (NO compiler on device — M5)
IMAGE_INSTALL:append = " packagegroup-joshua-middleware packagegroup-joshua-debug sudo"

# Hardware interface tools
IMAGE_INSTALL:append = " packagegroup-joshua-hardware vim"

# Product services remain available independently of the unavailable upstream
# Acadia repositories. Enable those recipes after supplying their sources and
# removing the BBMASK in the selected KAS configuration.
IMAGE_INSTALL:append = " acadia-services"

# Operator account — locked, SSH-key access only
inherit extrausers
EXTRA_USERS_PARAMS = "\
    useradd -m -s /bin/bash -G sudo rpi; \
    usermod -L rpi; \
"
