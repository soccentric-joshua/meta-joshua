SUMMARY = "Acadia persistent data directory initialisation"
DESCRIPTION = "Creates /home/acadia and provides a bind-mount unit so \
               acadia-client.service always finds /var/lib/acadia."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://acadia-data-init.service \
    file://var-lib-acadia.mount \
"

inherit allarch systemd

SYSTEMD_SERVICE:${PN} = " \
    acadia-data-init.service \
    var-lib-acadia.mount \
"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/acadia-data-init.service \
                    ${D}${systemd_system_unitdir}/acadia-data-init.service
    install -m 0644 ${WORKDIR}/var-lib-acadia.mount \
                    ${D}${systemd_system_unitdir}/var-lib-acadia.mount
    install -d ${D}/home/acadia
}

FILES:${PN} = " \
    ${systemd_system_unitdir}/acadia-data-init.service \
    ${systemd_system_unitdir}/var-lib-acadia.mount \
    /home/acadia \
"
