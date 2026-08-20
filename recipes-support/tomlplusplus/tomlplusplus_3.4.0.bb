# tomlplusplus — header-only TOML parser for C++17/20.
#
# Joshua-Client depends on toml++ (find_package(tomlplusplus REQUIRED) +
# tomlplusplus::tomlplusplus target) for parsing its TOML config file.
# toml++ is not packaged in the scarthgap meta-openembedded / poky layer set,
# so we carry a recipe here. It is header-only; the CMake build just installs
# headers + the exported config so the client's find_package works.

SUMMARY = "toml++ — C++17/20 TOML configuration parser (header-only)"
DESCRIPTION = "Header-only TOML library for C++17 and later, providing \
the tomlplusplus::tomlplusplus CMake target used by Joshua-Client."
HOMEPAGE = "https://github.com/marzer/tomlplusplus"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=90960f22c10049c117d56ed2ee5ee167"

SRC_URI = "https://github.com/marzer/tomlplusplus/archive/refs/tags/v3.4.0.tar.gz"
SRC_URI[sha256sum] = "8517f65938a4faae9ccf8ebb36631a38c1cadfb5efa85d9a72e15b9e97d25155"

S = "${WORKDIR}/tomlplusplus-3.4.0"

inherit cmake

# Install the headers + CMake config (exported target), skip examples/tests.
EXTRA_OECMAKE = " \
    -DTOMLPP_BUILD_INSTALL=ON \
    -DTOMLPP_BUILD_EXAMPLES=OFF \
    -DTOMLPP_BUILD_TESTS=OFF \
"

# Header-only: the -dev package carries the headers and CMake config.
FILES:${PN} = ""
FILES:${PN}-dev = " \
    ${includedir} \
    ${libdir}/cmake \
    ${datadir}/cmake \
    ${datadir}/tomlplusplus \
"
RDEPENDS:${PN}-dev = ""
