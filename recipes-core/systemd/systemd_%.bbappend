# Persistent journal + explicit NTP for Acadia devices.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += " \
    file://journald-acadia.conf \
    file://timesyncd-acadia.conf \
"

do_install:append() {
    install -d ${D}${sysconfdir}/systemd/journald.conf.d
    install -m 0644 ${WORKDIR}/journald-acadia.conf \
                    ${D}${sysconfdir}/systemd/journald.conf.d/acadia.conf

    # Do NOT pre-create /var/log/journal here: /var/log is a symlink to
    # /var/volatile/log in the sysroot, which triggers [empty-dirs] QA errors.
    # journald creates the directory automatically on first boot when
    # Storage=persistent is configured.

    install -d ${D}${sysconfdir}/systemd/timesyncd.conf.d
    install -m 0644 ${WORKDIR}/timesyncd-acadia.conf \
                    ${D}${sysconfdir}/systemd/timesyncd.conf.d/acadia.conf
}

FILES:${PN} += " \
    ${sysconfdir}/systemd/journald.conf.d/ \
    ${sysconfdir}/systemd/timesyncd.conf.d/ \
"

# Suppress the [empty-dirs] QA check for the upstream systemd-255 recipe which
# installs into /var/volatile on scarthgap (known upstream issue).
INSANE_SKIP:${PN} += "empty-dirs"
