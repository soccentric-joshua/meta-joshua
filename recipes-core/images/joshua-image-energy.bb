SUMMARY = "Joshua OS image for energy deployments"
DESCRIPTION = "Full-featured embedded Linux image for the TI Sitara AM62A/AM64x platform with A/B OTA, gRPC, and hardware interface tools, tuned for the energy vertical."
LICENSE = "MIT"

require recipes-core/images/joshua-image.bb
require recipes-core/images/joshua-image-common.inc

IMAGE_INSTALL:append = "     packagegroup-core-full-cmdline "
