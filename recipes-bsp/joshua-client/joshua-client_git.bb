# joshua-client — gRPC daemon for IV&V hardware validation.
#
# The Joshua-Client CMakeLists is structured as a "sibling build": it hardcodes
# `add_subdirectory(${CMAKE_SOURCE_DIR}/../Joshua-HAL ...)` and expects the HAL
# protos at `../Joshua-HAL/protos`. In the Yocto build we satisfy that contract
# by fetching the HAL tree into ${WORKDIR}/Joshua-HAL (destsuffix) so it sits
# one level above the client source, exactly where CMake looks for it.
# The image still installs the runtime HAL libraries from the separate
# joshua-hal recipe; this recipe only produces the joshua-client daemon.

SUMMARY = "Joshua Client — gRPC daemon for IV&V hardware validation"
DESCRIPTION = "Orchestrates hardware validation tests via 35+ module \
adapters, serializes HAL results to protobuf, spools them in SQLite, and \
streams them to a Joshua-Server instance over gRPC."
HOMEPAGE = "https://github.com/soccentric-joshua/Joshua-Client"
LICENSE = "CLOSED"
# Upstream is a private repo with no LICENSE file; BitBake requires a
# LIC_FILES_CHKSUM entry. We point at a stable local file shipped in SRC_URI.
LIC_FILES_CHKSUM = "file://LICENSE;md5=c2e1ba48eae067bc9e97d3bc6c8bcbfc"

SRC_URI = " \
    git://github.com/soccentric-joshua/Joshua-Client.git;protocol=ssh;user=git;branch=develop;destsuffix=git;name=client \
    git://github.com/soccentric-joshua/Joshua-HAL.git;protocol=ssh;user=git;branch=develop;destsuffix=Joshua-HAL;name=hal \
    file://LICENSE \
"
SRCREV_client = "${AUTOREV}"
SRCREV_hal = "${AUTOREV}"
SRCREV_FORMAT = "client_hal"

PV = "1.0+git${SRCPV}"
S = "${WORKDIR}/git"

inherit cmake systemd

DEPENDS = " \
    spdlog \
    tomlplusplus \
    yaml-cpp \
    sqlite3 \
    protobuf protobuf-native \
    grpc grpc-native \
    abseil-cpp \
    pkgconfig-native \
"

OECMAKE_SOURCEPATH = "${S}"
OECMAKE_EXTRA_OECMAKE = " \
    -DGRPC_CPP_PLUGIN=${STAGING_BINDIR_NATIVE}/grpc_cpp_plugin \
    -DBUILD_TESTS=OFF \
"

CXXFLAGS:append = " -Wno-error=unused-result"
LDFLAGS:append = " -Wl,--copy-dt-needed-entries"

# gRPC 1.60.1's CMake config does not export the gRPC::grpc_cpp_plugin target
# (added in gRPC 1.62). Patch BOTH the client's top-level CMakeLists and the
# HAL protos/CMakeLists.txt (built via add_subdirectory(../Joshua-HAL)) to
# locate the plugin by name instead. (Same fix the standalone joshua-hal recipe
# applies to the HAL protos.)
do_configure:prepend() {
    sed -i '/get_target_property(GRPC_CPP_PLUGIN gRPC::grpc_cpp_plugin LOCATION)/c\
set(GRPC_CPP_PLUGIN "${GRPC_CPP_PLUGIN}")\
find_program(GRPC_CPP_PLUGIN grpc_cpp_plugin\
    HINTS ${gRPC_BIN_DIR} /usr/local/bin /usr/bin /usr/lib/grpc\
          /usr/lib/aarch64-linux-gnu /usr/lib/x86_64-linux-gnu\
    DOC "Path to the grpc_cpp_plugin protoc plugin")' ${S}/CMakeLists.txt
    sed -i '/get_target_property(GRPC_CPP_PLUGIN gRPC::grpc_cpp_plugin LOCATION)/c\
find_program(GRPC_CPP_PLUGIN grpc_cpp_plugin\
    HINTS ${gRPC_BIN_DIR} /usr/local/bin /usr/bin /usr/lib/grpc\
          /usr/lib/aarch64-linux-gnu /usr/lib/x86_64-linux-gnu\
    DOC "Path to the grpc_cpp_plugin protoc plugin")\
if(NOT GRPC_CPP_PLUGIN)\
  message(FATAL_ERROR "[hal_protos] grpc_cpp_plugin not found via CMake config")\
endif()' ${WORKDIR}/Joshua-HAL/protos/CMakeLists.txt
}

# CMake's install() already places the joshua-client binary. We additionally
# install the hardened systemd unit so the daemon starts on boot.
do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${S}/systemd/joshua-client.service \
                    ${D}${systemd_system_unitdir}/joshua-client.service
}

FILES:${PN} += "${systemd_system_unitdir}/joshua-client.service"
SYSTEMD_SERVICE:${PN} = "joshua-client.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
