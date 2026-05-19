SUMMARY = "Acadia Client daemon — gRPC client for the Acadia IV&V server"
DESCRIPTION = "C++ daemon that connects to an external Acadia server via gRPC, \
               runs hardware validation tests using Acadia-HAL, and reports results."
HOMEPAGE = "https://github.com/SiliconCentric/Acadia-Client"
LICENSE = "CLOSED"

SRC_URI = " \
    git://git@github.com/SiliconCentric/Acadia-Client.git;protocol=ssh;user=git;branch=release \
    file://acadia-client.service \
    file://0001-fix-grpc-cpp-plugin-cross-compile.patch \
"
# TODO: replace with the real commit SHA once the upstream repo exists:
#   SRCREV = "<40-char-sha>"
# Using a hardcoded placeholder avoids a parse-time network fetch (AUTOREV
# forces BitBake to contact the remote during parsing, breaking offline builds).
SRCREV = "71ee04ec3f9bb06cce989baae6de6f37148fb50d"

PV = "1.0+git${SRCPV}"
S = "${WORKDIR}/git"

inherit cmake systemd useradd

OECMAKE_SOURCEPATH = "${S}"

DEPENDS += "grpc grpc-native protobuf spdlog tomlplusplus yaml-cpp sqlite3"
RDEPENDS:${PN} = "grpc spdlog yaml-cpp libsqlite3"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-r -s /sbin/nologin -d /var/lib/acadia -g acadia acadia"
GROUPADD_PARAM:${PN} = "acadia"

SYSTEMD_SERVICE:${PN}     = "acadia-client.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/acadia-client.service \
                    ${D}${systemd_system_unitdir}/acadia-client.service
}

FILES:${PN} += "${systemd_system_unitdir}/acadia-client.service"
