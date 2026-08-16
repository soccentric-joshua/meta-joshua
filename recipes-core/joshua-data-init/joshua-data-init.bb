SUMMARY = "Joshua persistent home directory initialisation"
DESCRIPTION = "Creates /home/joshua on first boot after the persistent home \
partition (p4) is mounted at /home."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://joshua-data-init.sh \
    file://joshua-data-init.service \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN}     = "joshua-data-init.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash util-linux parted e2fsprogs-resize2fs"

do_install() {
    install -d ${D}${libdir}/joshua-data-init
    install -m 0755 ${WORKDIR}/joshua-data-init.sh \
                    ${D}${libdir}/joshua-data-init/joshua-data-init.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/joshua-data-init.service \
                    ${D}${systemd_system_unitdir}/joshua-data-init.service
}

FILES:${PN} += " \
    ${libdir}/joshua-data-init/ \
    ${systemd_system_unitdir}/joshua-data-init.service \
"
