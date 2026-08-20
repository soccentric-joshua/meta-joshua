# joshua-configuration — on-device web configuration daemon (WebConfig).
#
# Rust cargo workspace (crates/webconfigd daemon + webconfig-engine). Built
# offline with cargo.bbclass against the committed Cargo.lock; the full crate
# list is generated into joshua-configuration-crates.inc (bitbake -c
# update_crates joshua-configuration). Installs the daemon, the no-build
# vanilla-JS frontend, schema files, the board manifest, and the hardened
# systemd unit + polkit rule.

SUMMARY = "Joshua Configuration — on-device web configuration daemon"
DESCRIPTION = "Browser-based device configuration UI for embedded Linux; \
one Rust daemon serving a no-build vanilla-JS frontend over TLS, with \
schema-driven settings and D-Bus/polkit for privileged operations."
HOMEPAGE = "https://github.com/soccentric-joshua/Joshua-Configuration"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3382002c026d2dcb5817068a8f184313"

SRC_URI = " \
    git://github.com/soccentric-joshua/Joshua-Configuration.git;protocol=ssh;user=git;branch=develop;destsuffix=git \
    file://webconfigd.service \
    file://webconfigd.env \
    file://50-webconfigd.polkit.rules \
    file://app-example.schema.json \
    file://app-mcu-stm32.schema.json \
"
# All crates.io dependencies (generated from the committed Cargo.lock).
require joshua-configuration-crates.inc

SRCREV = "${AUTOREV}"
PV = "0.1.0+git${SRCPV}"
S = "${WORKDIR}/git"

inherit cargo pkgconfig systemd useradd

# The prebuilt rust-std is compiled for the standard upstream triples, not
# poky's custom <arch>-<vendor>-linux-gnu triples.  Use the standard aarch64
# target so rustc accepts the prebuilt standard library.
RUST_HOST_SYS = "aarch64-unknown-linux-gnu"
RUST_TARGET_SYS = "aarch64-unknown-linux-gnu"

# The project's Cargo.lock is v4 (rust-version 1.85) but the scarthgap
# toolchain ships cargo 1.75, which only reads v3 lockfiles. Rewrite the
# header (content is otherwise compatible, checksums present) and bypass the
# rust-version gate so cargo 1.75 can attempt the build.
CARGO_BUILD_FLAGS:append = " --ignore-rust-version"

do_compile:prepend() {
    sed -i 's/^version = 4$/version = 3/' ${S}/Cargo.lock
}

# --- Runtime user (non-root, hardened) --------------------------------------
WEBCONFIG_USER = "webconfig"
WEBCONFIG_GROUP = "webconfig"
USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-r -M -g ${WEBCONFIG_GROUP} -d /var/lib/webconfig -s /usr/sbin/nologin ${WEBCONFIG_USER}"
GROUPADD_PARAM:${PN} = "-r ${WEBCONFIG_GROUP}"

do_install:append() {
    # Daemon binary (cargo.bbclass default: release build, frozen).
    install -d ${D}${bindir}
    install -m 0755 ${B}/target/${CARGO_TARGET_SUBDIR}/webconfigd ${D}${bindir}/webconfigd

    # Data partition owned by the runtime user.
    install -d -o ${WEBCONFIG_USER} -g ${WEBCONFIG_GROUP} ${D}/var/lib/webconfig

    # Hardened systemd unit + default environment.
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/webconfigd.service ${D}${systemd_system_unitdir}/
    install -d ${D}${sysconfdir}/default
    install -m 0644 ${WORKDIR}/webconfigd.env ${D}${sysconfdir}/default/webconfigd

    # polkit rule for the daemon's limited D-Bus actions.
    install -d ${D}${datadir}/polkit-1/rules.d
    install -m 0644 ${WORKDIR}/50-webconfigd.polkit.rules \
        ${D}${datadir}/polkit-1/rules.d/50-webconfigd.rules

    # No-build frontend static assets (module/CSS import graph must stay intact).
    install -d ${D}${datadir}/webconfig/static
    cp -a --no-preserve=ownership ${S}/frontend/. ${D}${datadir}/webconfig/static/
    test -f ${D}${datadir}/webconfig/static/index.html || \
        bbfatal "joshua-configuration: frontend/index.html missing from source"

    # Schema files (schema-driven rendering + validation).
    install -d ${D}${datadir}/webconfig/schemas
    install -m 0644 ${WORKDIR}/app-example.schema.json ${D}${datadir}/webconfig/schemas/
    install -m 0644 ${WORKDIR}/app-mcu-stm32.schema.json ${D}${datadir}/webconfig/schemas/

    # Board manifest.
    install -d ${D}${datadir}/webconfig
    install -m 0644 ${S}/board-manifests/joshua.json \
        ${D}${datadir}/webconfig/board-manifest.json
}

FILES:${PN} += " \
    ${bindir}/webconfigd \
    ${systemd_system_unitdir}/webconfigd.service \
    ${sysconfdir}/default/webconfigd \
    ${datadir}/polkit-1/rules.d/50-webconfigd.rules \
    ${datadir}/webconfig/static \
    ${datadir}/webconfig/schemas \
    ${datadir}/webconfig/board-manifest.json \
    /var/lib/webconfig \
"

# cargo builds with -C strip=symbols, so the binary ships pre-stripped.
INSANE_SKIP:${PN} += "already-stripped"

SYSTEMD_SERVICE:${PN} = "webconfigd.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "ca-certificates"
RRECOMMENDS:${PN} = "systemd"
