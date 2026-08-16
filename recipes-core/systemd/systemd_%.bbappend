# Persistent journal + explicit NTP for Joshua devices.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
    file://journald-joshua.conf \
    file://timesyncd-joshua.conf \
"

do_install:append() {
    install -d ${D}${sysconfdir}/systemd/journald.conf.d
    install -m 0644 ${WORKDIR}/journald-joshua.conf \
                    ${D}${sysconfdir}/systemd/journald.conf.d/joshua.conf

    install -d ${D}${sysconfdir}/systemd/timesyncd.conf.d
    install -m 0644 ${WORKDIR}/timesyncd-joshua.conf \
                    ${D}${sysconfdir}/systemd/timesyncd.conf.d/joshua.conf
}

FILES:${PN} += " \
    ${sysconfdir}/systemd/journald.conf.d/ \
    ${sysconfdir}/systemd/timesyncd.conf.d/ \
"

INSANE_SKIP:${PN} += "empty-dirs"
