SUMMARY = "Acadia Hardware Abstraction Layer"
DESCRIPTION = "HAL library for the Acadia platform, providing hardware abstraction interfaces."
HOMEPAGE = "https://github.com/SiliconCentric/Acaida-HAL"
LICENSE = "CLOSED"

SRC_URI = "git://github.com/SiliconCentric/Acaida-HAL.git;protocol=ssh;user=git;branch=master"
SRCREV = "${AUTOREV}"

PV = "1.0+git${SRCPV}"
S = "${WORKDIR}/git"

inherit cmake

OECMAKE_SOURCEPATH = "${S}"

# Upstream source uses -Werror but has intentionally-ignored return values
# for write()/read()/system() calls in test code; suppress the error promotion
# for that specific warning only.
CXXFLAGS:append = " -Wno-error=unused-result"

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

# Unversioned .so files are placed in the main package (not -dev) because the
# upstream project does not produce versioned shared library names.
FILES:${PN} += "${libdir}/*.so"
INSANE_SKIP:${PN} += "dev-so"
