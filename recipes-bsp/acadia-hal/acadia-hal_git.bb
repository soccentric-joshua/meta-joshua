SUMMARY = "Acadia Hardware Abstraction Layer"
DESCRIPTION = "HAL library for the Acadia platform, providing hardware abstraction interfaces."
HOMEPAGE = "https://github.com/SiliconCentric/Acadia-HAL"
LICENSE = "CLOSED"

SRC_URI = "git://github.com/SiliconCentric/Acadia-HAL.git;protocol=https;branch=master"
SRCREV = "${AUTOREV}"

PV = "1.0+git${SRCPV}"
S = "${WORKDIR}/git"

inherit cmake

DEPENDS = "protobuf protobuf-native grpc grpc-native abseil-cpp pkgconfig-native"

OECMAKE_SOURCEPATH = "${S}"

# Explicitly point CMake at the host-native grpc_cpp_plugin so it is never
# resolved to the cross-compiled (aarch64) binary from the target sysroot.
OECMAKE_EXTRA_OECMAKE = "-DGRPC_CPP_PLUGIN=${STAGING_BINDIR_NATIVE}/grpc_cpp_plugin"

# When cross-compiling, gRPCConfig.cmake skips gRPCPluginTargets.cmake
# (guarded by `if(NOT CMAKE_CROSSCOMPILING)`), so gRPC::grpc_cpp_plugin is
# never defined and the unconditional get_target_property() call fatally
# errors.  Guard it with a TARGET check so the pkg-config fallback path
# (which uses find_program and is then overridden by -DGRPC_CPP_PLUGIN above)
# is taken instead.
do_configure:prepend() {
    sed -i \
        's|get_target_property(GRPC_CPP_PLUGIN gRPC::grpc_cpp_plugin LOCATION)|if(TARGET gRPC::grpc_cpp_plugin)\n        get_target_property(GRPC_CPP_PLUGIN gRPC::grpc_cpp_plugin LOCATION)\n    else()\n        find_program(GRPC_CPP_PLUGIN grpc_cpp_plugin REQUIRED)\n    endif()|' \
        "${S}/protos/CMakeLists.txt"
}

# Upstream source uses -Werror but has intentionally-ignored return values
# for write()/read()/system() calls in test code; suppress the error promotion
# for that specific warning only.
CXXFLAGS:append = " -Wno-error=unused-result"

# gRPC carries abseil as a private (non-propagated) dependency.  Static
# archives built against gRPC therefore have unresolved abseil symbols at
# final link time.  --copy-dt-needed-entries makes the linker follow
# transitive shared-library dependencies automatically, resolving those refs.
LDFLAGS:append = " -Wl,--copy-dt-needed-entries"

# The upstream CMakeLists.txt has no install() rules; install artifacts manually.
do_install() {
    # Shared libraries → runtime package
    install -d ${D}${libdir}
    find ${B}/lib -maxdepth 1 -name "*.so*" -exec install -m 0755 {} ${D}${libdir}/ \;

    # Static libraries → staticdev package
    find ${B}/lib -maxdepth 1 -name "*.a" -exec install -m 0644 {} ${D}${libdir}/ \;

    # Test / diagnostic executables → main package
    install -d ${D}${bindir}
    find ${B}/bin -maxdepth 1 -type f -exec install -m 0755 {} ${D}${bindir}/ \;

    # Public headers → dev package
    install -d ${D}${includedir}/${PN}
    find ${S}/src -name "*.h" -o -name "*.hpp" | while read f; do
        install -m 0644 "$f" ${D}${includedir}/${PN}/
    done
}

# Unversioned .so files: upstream produces no versioned .so names.
# Move them to the main package (not -dev) and suppress the related QA checks.
FILES:${PN} += "${libdir}/*.so"
INSANE_SKIP:${PN} += "dev-so"
# BitBake still places non-symlink .so in -dev by default; suppress that check too.
INSANE_SKIP:${PN}-dev += "dev-elf"
