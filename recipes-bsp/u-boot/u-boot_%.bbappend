FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

# Provide the boot script source that u-boot.inc compiles into boot.scr
# when UBOOT_ENV = "boot" and UBOOT_ENV_SUFFIX = "scr" are set.
SRC_URI:append:rpi = " file://boot.cmd"

# u-boot now compiles and deploys boot.scr itself; remove the dependency on
# rpi-u-boot-scr (which also deploys boot.scr) to avoid duplicate-file conflicts.
DEPENDS:remove = "u-boot-default-script"
