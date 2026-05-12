# Enable RSA signed-image verification at compile time (C2).
# swupdate uses Kconfig (cml1), not PACKAGECONFIG; add a config fragment.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " file://acadia-signing.cfg"
