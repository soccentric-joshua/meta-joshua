# Harden the OpenSSH server: key-based auth only, no passwords, no root login.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://acadia-hardening.conf"

do_install:append() {
    install -d ${D}${sysconfdir}/ssh/sshd_config.d
    install -m 0644 ${WORKDIR}/acadia-hardening.conf \
                    ${D}${sysconfdir}/ssh/sshd_config.d/acadia-hardening.conf

    # Prepend Include directive to sshd_config if not already present.
    if ! grep -q 'Include.*sshd_config\.d' ${D}${sysconfdir}/ssh/sshd_config 2>/dev/null; then
        sed -i '1s|^|Include /etc/ssh/sshd_config.d/*.conf\n|' \
            ${D}${sysconfdir}/ssh/sshd_config || true
    fi
}

FILES:${PN} += "${sysconfdir}/ssh/sshd_config.d/"
