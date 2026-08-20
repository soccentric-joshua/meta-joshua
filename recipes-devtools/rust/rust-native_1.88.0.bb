# Prebuilt Rust compiler (native / build host).
#
# Joshua-Configuration's committed Cargo.lock (rust-version 1.85, edition2024
# crates) requires a toolchain newer than the scarthgap poky default (1.75).
# Instead of building Rust from source, install the official prebuilt
# x86_64-unknown-linux-gnu rustc (which also carries the host standard
# library).  This replaces poky's rust_1.75.0.bb rust-native and is selected
# via PREFERRED_VERSION_rust-native in the build config.
#
# NOTE: rust 1.85.x segfaults under poky's uninative loader (glibc 2.43); 1.88
# runs cleanly, so we pin >=1.88.

SUMMARY = "Rust compiler and host standard library (prebuilt, native)"
HOMEPAGE = "https://www.rust-lang.org"
SECTION = "devel"
LICENSE = "MIT | Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Pinned rust 1.88.0 release (2025-06-26). The rustc tarball bundles rustc,
# rustdoc and rust-demangler; the separate rust-std tarball provides the host
# (x86_64) standard library needed to build build-scripts/proc-macros.
SRC_URI = " \
    https://static.rust-lang.org/dist/2025-06-26/rustc-1.88.0-x86_64-unknown-linux-gnu.tar.gz \
    https://static.rust-lang.org/dist/2025-06-26/rust-std-1.88.0-x86_64-unknown-linux-gnu.tar.gz;name=ruststd \
"
SRC_URI[sha256sum] = "270549d2520ce0fee736c07266fe154d47a5660774546bd2c3b1a2ad737a9e6d"
SRC_URI[ruststd.sha256sum] = "0d7967f18bbf29777843248091a4f06cdfec2500de33cbfe6b69c6af255f72b7"

S = "${WORKDIR}/rustc-1.88.0-x86_64-unknown-linux-gnu"

inherit native

# This standalone -native recipe isn't processed by native_virtclass_handler
# (no BBCLASSEXTEND), so STAGING_DIR_NATIVE isn't cleared for the in-situ
# native install.  Clear it so prefix=/usr and files land in ${D}/usr for
# sysroot_stage_dirs to pick up.
STAGING_DIR_NATIVE = ""

# Stripping the prebuilt rust binaries/libs (librustc_driver, libLLVM) breaks
# them: cargo spawns rustc via posix_spawn and the stripped driver segfaults.
# Ship them unstripped.
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_STRIP = "1"

do_install() {
    ${S}/install.sh --prefix="${prefix}" --destdir="${D}" --disable-ldconfig --disable-verify

    # Install the host (x86_64) standard library so rustc can build
    # build-scripts / proc-macros for the build machine.
    cp -a --no-preserve=ownership \
        ${WORKDIR}/rust-std-1.88.0-x86_64-unknown-linux-gnu/rust-std-x86_64-unknown-linux-gnu/lib/rustlib/. \
        ${D}${prefix}/lib/rustlib/

    # Drop installer bookkeeping that conflicts with other installables.
    rm -f ${D}${prefix}/lib/rustlib/uninstall.sh
    rm -f ${D}${prefix}/lib/rustlib/install.log
    rm -f ${D}${prefix}/lib/rustlib/components
    rm -f ${D}${prefix}/lib/rustlib/rust-installer-version
    rm -rf ${D}${prefix}/lib/rustlib/manifest*
}
