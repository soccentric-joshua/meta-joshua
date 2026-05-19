SUMMARY = "Header-only C++ TOML parser and serialiser, conforming to TOML v1.0"
DESCRIPTION = "toml++ is a header-only TOML library for C++17 or later."
HOMEPAGE = "https://github.com/marzer/tomlplusplus"
SECTION = "devel"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=90960f22c10049c117d56ed2ee5ee167"

SRCREV = "30172438cee64926dc41fdd9c11fb3ba5b2ba9de"
SRC_URI = "git://github.com/marzer/tomlplusplus.git;protocol=https;nobranch=1"

PV = "3.4.0"
S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE = "-DTOMLPLUSPLUS_BUILD_TESTS=OFF -DTOMLPLUSPLUS_BUILD_EXAMPLES=OFF"

# toml++ is header-only; no runtime library is produced.
ALLOW_EMPTY:${PN} = "1"
FILES:${PN}     = ""
FILES:${PN}-dev = "${includedir} ${libdir}/cmake ${datadir}/cmake ${libdir}/pkgconfig"
