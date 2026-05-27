SUMMARY = "Initialise /home on first boot after the persistent home partition is mounted"
DESCRIPTION = "\
On first boot the empty ext4 home partition (p4) is mounted at /home, hiding \
the skeleton copy that useradd placed in the rootfs.  This one-shot service \
creates /home/joshua with correct ownership and populates it from /etc/skel so \
the user can log in immediately."

LICENSE     = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://joshua-home-init.sh \
    file://joshua-home-init.service \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN}     = "joshua-home-init.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

RDEPENDS:${PN} = "bash util-linux parted e2fsprogs-resize2fs"

do_install() {
    install -d ${D}${libdir}/joshua-home-init
    install -m 0755 ${WORKDIR}/joshua-home-init.sh \
                    ${D}${libdir}/joshua-home-init/joshua-home-init.sh

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/joshua-home-init.service \
                    ${D}${systemd_system_unitdir}/joshua-home-init.service
}

FILES:${PN} += " \
    ${libdir}/joshua-home-init/ \
    ${systemd_system_unitdir}/joshua-home-init.service \
"
