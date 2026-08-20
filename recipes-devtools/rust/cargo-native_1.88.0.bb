# Prebuilt Cargo (native / build host).
#
# Companion to rust-native_1.88.0.bb — the official prebuilt cargo 1.88.0 for
# the x86_64 build host.  Replaces poky's cargo_1.75.0.bb cargo-native,
# selected via PREFERRED_VERSION_cargo-native.

SUMMARY = "Cargo package manager for Rust (prebuilt, native)"
HOMEPAGE = "https://www.rust-lang.org"
SECTION = "devel"
LICENSE = "MIT | Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://static.rust-lang.org/dist/2025-06-26/cargo-1.88.0-x86_64-unknown-linux-gnu.tar.gz"
SRC_URI[sha256sum] = "3ddcf45a41114479d0267b22cc52f7f7b9fae9aad9bf433ac5de5e04f15919c4"

S = "${WORKDIR}/cargo-1.88.0-x86_64-unknown-linux-gnu"

inherit native

# Standalone -native recipe: clear STAGING_DIR_NATIVE so prefix=/usr and files
# land in ${D}/usr for sysroot_stage_dirs (see rust-native_1.88.0.bb).
STAGING_DIR_NATIVE = ""

# Do not strip (see rust-native_1.88.0.bb).
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_STRIP = "1"

do_install() {
    ${S}/install.sh --prefix="${prefix}" --destdir="${D}" --disable-ldconfig --disable-verify
}
