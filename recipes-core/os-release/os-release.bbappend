# os-release_%.bbappend — T052
#
# Overwrite the distro identification fields so that /etc/os-release on the
# running system identifies this image as "Acadia OS".
#
# Yocto's os-release recipe honours the DISTRO_* variables, so setting them
# here (after the base recipe runs) produces the correct /etc/os-release
# without needing to touch the upstream recipe.

# Human-readable name shown by neofetch, systemd, etc.
DISTRO_NAME  = "Acadia OS"
DISTRO_VERSION = "${PV}"

# Fields written to /etc/os-release by the upstream recipe:
#   PRETTY_NAME  → "Acadia OS <version>"
#   NAME         → "Acadia OS"
#   VERSION_ID   → "<version>"
#   ID           → "acadia"
#   ID_LIKE      → "poky"
PRETTY_NAME = "Acadia OS ${PV}"
OS_RELEASE_FIELDS:append = " ID_LIKE"
ID_LIKE = "poky"

# The upstream os-release recipe writes these variables directly; override:
do_install:append() {
    # Rewrite the generated /etc/os-release with Acadia-specific content.
    # This runs after the base recipe's do_install so it wins.
    cat > ${D}${sysconfdir}/os-release <<OSEOF
ID=acadia
ID_LIKE=poky
NAME="Acadia OS"
VERSION="${PV}"
VERSION_ID="${PV}"
PRETTY_NAME="Acadia OS ${PV}"
HOME_URL="https://acadia.dev/"
SUPPORT_URL="https://acadia.dev/support"
BUG_REPORT_URL="https://acadia.dev/issues"
OSEOF
}
