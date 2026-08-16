# os-release_%.bbappend — identify this image as "Joshua OS".
DISTRO_NAME  = "Joshua OS"
DISTRO_VERSION = "${PV}"
PRETTY_NAME = "Joshua OS ${PV}"
OS_RELEASE_FIELDS:append = " ID_LIKE"
ID_LIKE = "poky"

do_install:append() {
    cat > ${D}${sysconfdir}/os-release <<OSEOF
ID=joshua
ID_LIKE=poky
NAME="Joshua OS"
VERSION="${PV}"
VERSION_ID="${PV}"
PRETTY_NAME="Joshua OS ${PV}"
HOME_URL="https://joshua.dev/"
SUPPORT_URL="https://joshua.dev/support"
BUG_REPORT_URL="https://joshua.dev/issues"
OSEOF
}
