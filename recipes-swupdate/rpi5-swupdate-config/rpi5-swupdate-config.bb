SUMMARY     = "SWUpdate runtime configuration and A/B OTA helper scripts for RPi5"
DESCRIPTION = "Installs swupdate.cfg, slot-detection / boot-slot scripts, a \
               systemd drop-in that enables the Mongoose web UI on port 8080, \
               boot-watchdog for rollback counter, health-check service, \
               startup wrapper with optional signing + web UI auth."
LICENSE     = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://swupdate.cfg \
    file://pre-install.sh \
    file://post-install.sh \
    file://swupdate-bootcheck.sh \
    file://swupdate-bootcheck.service \
    file://swupdate-webui.conf \
    file://boot-watchdog.sh \
    file://boot-watchdog.service \
    file://swupdate-start.sh \
    file://swupdate-webui-auth-init.sh \
    file://swupdate-webui-auth-init.service \
    file://bootcount-reset.sh \
    file://bootcount-reset.service \
    file://fw_env.config \
"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_SERVICE:${PN} = " \
    swupdate-bootcheck.service \
    boot-watchdog.service \
    swupdate-webui-auth-init.service \
    bootcount-reset.service \
"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# swupdate and bash are required at runtime; util-linux provides findmnt/blkid;
# openssl is needed by swupdate-webui-auth-init.sh for password generation;
# libubootenv provides fw_setenv/fw_printenv for U-Boot env OTA confirmation.
RDEPENDS:${PN} = "swupdate bash util-linux openssl libubootenv"

do_install() {
    # swupdate main config
    install -d ${D}${sysconfdir}/swupdate
    install -m 0644 ${WORKDIR}/swupdate.cfg \
                    ${D}${sysconfdir}/swupdate/swupdate.cfg

    # A/B slot helper scripts (also shipped in .swu packages)
    install -d ${D}${libdir}/swupdate
    install -m 0755 ${WORKDIR}/pre-install.sh \
                    ${D}${libdir}/swupdate/pre-install.sh
    install -m 0755 ${WORKDIR}/post-install.sh \
                    ${D}${libdir}/swupdate/post-install.sh
    install -m 0755 ${WORKDIR}/swupdate-bootcheck.sh \
                    ${D}${libdir}/swupdate/swupdate-bootcheck.sh

    # Boot watchdog (C1 — runs before basic.target)
    install -d ${D}${sbindir}
    install -m 0755 ${WORKDIR}/boot-watchdog.sh \
                    ${D}${sbindir}/boot-watchdog.sh

    # T051: U-Boot env bootcount-reset script and config
    install -m 0755 ${WORKDIR}/bootcount-reset.sh \
                    ${D}${libdir}/swupdate/bootcount-reset.sh
    install -m 0644 ${WORKDIR}/fw_env.config \
                    ${D}${sysconfdir}/fw_env.config

    # swupdate startup wrapper (C2 signing + I7 web UI auth)
    install -m 0755 ${WORKDIR}/swupdate-start.sh \
                    ${D}${sbindir}/swupdate-start.sh

    # First-boot credential generator (I7)
    install -m 0755 ${WORKDIR}/swupdate-webui-auth-init.sh \
                    ${D}${sbindir}/swupdate-webui-auth-init.sh

    # systemd units
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/swupdate-bootcheck.service \
                    ${D}${systemd_system_unitdir}/swupdate-bootcheck.service
    install -m 0644 ${WORKDIR}/boot-watchdog.service \
                    ${D}${systemd_system_unitdir}/boot-watchdog.service
    install -m 0644 ${WORKDIR}/swupdate-webui-auth-init.service \
                    ${D}${systemd_system_unitdir}/swupdate-webui-auth-init.service
    install -m 0644 ${WORKDIR}/bootcount-reset.service \
                    ${D}${systemd_system_unitdir}/bootcount-reset.service

    # Drop-in: replace ExecStart with swupdate-start.sh wrapper
    install -d ${D}${systemd_system_unitdir}/swupdate.service.d
    install -m 0644 ${WORKDIR}/swupdate-webui.conf \
                    ${D}${systemd_system_unitdir}/swupdate.service.d/webui.conf
}

FILES:${PN} += " \
    ${sysconfdir}/swupdate/ \
    ${sysconfdir}/fw_env.config \
    ${libdir}/swupdate/ \
    ${sbindir}/boot-watchdog.sh \
    ${sbindir}/swupdate-start.sh \
    ${sbindir}/swupdate-webui-auth-init.sh \
    ${systemd_system_unitdir}/swupdate-bootcheck.service \
    ${systemd_system_unitdir}/boot-watchdog.service \
    ${systemd_system_unitdir}/swupdate-webui-auth-init.service \
    ${systemd_system_unitdir}/bootcount-reset.service \
    ${systemd_system_unitdir}/swupdate.service.d/ \
"
