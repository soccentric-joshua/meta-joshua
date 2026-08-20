# Prebuilt Rust standard library for the target (aarch64).
#
# Companion to rust-native_1.88.0.bb / cargo-native_1.88.0.bb.  Poko's cargo
# class cross-compiles with --target <RUST_HOST_SYS> and links against a target
# standard library installed under ${rustlibdir} (the libstd-rs recipe).  This
# recipe installs the official prebuilt aarch64 rust-std (rlib/rmeta artifacts)
# under the poky triple name so rustc 1.88 can find them.  Replaces poky's
# libstd-rs_1.75.0.bb via PREFERRED_VERSION_libstd-rs.

SUMMARY = "Rust standard library for the target (prebuilt)"
HOMEPAGE = "https://www.rust-lang.org"
SECTION = "devel"
LICENSE = "(MIT | Apache-2.0) & Unicode-DFS-2016"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://static.rust-lang.org/dist/2025-06-26/rust-std-1.88.0-aarch64-unknown-linux-gnu.tar.gz"
SRC_URI[sha256sum] = "91f61c01a960c37ae1032c4450d329253112ab46f02d61fe0203b366d75527c0"

S = "${WORKDIR}/rust-std-1.88.0-aarch64-unknown-linux-gnu"

inherit rust-common

# Match the standard upstream aarch64 triple used by the prebuilt rust-std (see
# joshua-configuration_git.bb).
RUST_HOST_SYS = "aarch64-unknown-linux-gnu"
RUST_TARGET_SYS = "aarch64-unknown-linux-gnu"

RUSTLIB_DEP = ""

# The prebuilt rust-std ships static archives (librustc-stable_rt.*.a) and
# .rlib artifacts that must live in the runtime sysroot for cross-linking;
# they are not meant to be split into -staticdev.
INSANE_SKIP:${PN} = "staticdev"

do_install() {
    # The prebuilt std is laid out under the upstream aarch64-unknown-linux-gnu
    # triple (with a nested rust-std-<triple> dir); stage it under the poky
    # triple so the consumer's --target aarch64-<vendor>-linux-gnu lookup
    # (-L ${rustlibdir}) resolves.
    install -d ${D}${rustlibdir}
    cp -a --no-preserve=ownership \
        ${S}/rust-std-aarch64-unknown-linux-gnu/lib/rustlib/aarch64-unknown-linux-gnu/lib/. \
        ${D}${rustlibdir}/
}

FILES:${PN} += "${rustlibdir}"
