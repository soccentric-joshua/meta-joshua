SUMMARY = "Joshua Hardware Abstraction Layer"
DESCRIPTION = "HAL library for the Joshua platform (Zynq UltraScale+ MPSoC), \
providing hardware abstraction interfaces."
HOMEPAGE = "https://github.com/SiliconCentric/Joshua-HAL"
LICENSE = "CLOSED"
# Upstream is a private repo; no LICENSE file is shipped. BitBake requires
# a LIC_FILES_CHKSUM entry. We point at a stable local file.
LIC_FILES_CHKSUM = "file://security.md;md5=1606e3c70e7eb33ddfd7fa686c3b5ff0"

SRC_URI = " \
    git://github.com/SiliconCentric/Joshua-HAL.git;protocol=ssh;user=git;branch=develop \
    file://security.md \
"
SRCREV = "${AUTOREV}"

PV = "1.0+git${SRCPV}"
S = "${WORKDIR}/git"

inherit cmake

DEPENDS = "protobuf protobuf-native grpc grpc-native abseil-cpp pkgconfig-native"

OECMAKE_SOURCEPATH = "${S}"
OECMAKE_EXTRA_OECMAKE = "-DGRPC_CPP_PLUGIN=${STAGING_BINDIR_NATIVE}/grpc_cpp_plugin"

CXXFLAGS:append = " -Wno-error=unused-result"
LDFLAGS:append = " -Wl,--copy-dt-needed-entries"

# gRPC 1.60.1's CMake config does not export the gRPC::grpc_cpp_plugin target
# (added in gRPC 1.62). Replace the get_target_property call with find_program
# so the plugin binary is located by name instead.
do_configure:prepend() {
    sed -i '/get_target_property(GRPC_CPP_PLUGIN gRPC::grpc_cpp_plugin LOCATION)/c\
find_program(GRPC_CPP_PLUGIN grpc_cpp_plugin\
    HINTS ${gRPC_BIN_DIR} /usr/local/bin /usr/bin /usr/lib/grpc\
          /usr/lib/aarch64-linux-gnu /usr/lib/x86_64-linux-gnu\
    DOC "Path to the grpc_cpp_plugin protoc plugin")\
if(NOT GRPC_CPP_PLUGIN)\
  message(FATAL_ERROR "[hal_protos] grpc_cpp_plugin not found via CMake config")\
endif()' ${S}/protos/CMakeLists.txt
}

do_install() {
    install -d ${D}${libdir}
    find ${B}/lib -maxdepth 1 -name "*.so*" -exec install -m 0755 {} ${D}${libdir}/ \;
    find ${B}/lib -maxdepth 1 -name "*.a" -exec install -m 0644 {} ${D}${libdir}/ \;

    install -d ${D}${bindir}
    find ${B}/bin -maxdepth 1 -type f -exec install -m 0755 {} ${D}${bindir}/ \;

    install -d ${D}${includedir}/${PN}
    find ${S}/src -name "*.h" -o -name "*.hpp" | while read f; do
        install -m 0644 "$f" ${D}${includedir}/${PN}/
    done

    install -d ${D}${docdir}/${PN}
    install -m 0644 ${WORKDIR}/security.md ${D}${docdir}/${PN}/security.md
}

FILES:${PN}      += "${libdir}/*.so"
FILES:${PN}-dev  = "${includedir} ${libdir}/cmake ${libdir}/pkgconfig ${datadir}/cmake ${datadir}/${BPN}"
INSANE_SKIP:${PN} += "dev-so"

PRIVATE_LIBS = "libjoshua-hal*.so"
