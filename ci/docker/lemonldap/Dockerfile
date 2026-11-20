FROM lemonldapng/lemonldap-ng:2.22.2
ENV TZ=Europe/Paris

COPY common/fr.json usr/share/lemonldap-ng/portal/templates/common/mail/fr.json
COPY common/mail_confirm.tpl /usr/share/lemonldap-ng/portal/templates/common/common/mail_confirm.tpl
COPY common/mail_footer.tpl /usr/share/lemonldap-ng/portal/templates/common/common/mail_footer.tpl
COPY common/mail_header.tpl /usr/share/lemonldap-ng/portal/templates/common/common/mail_header.tpl