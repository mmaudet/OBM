# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name:           obm
Version:        %{obm_version}
Release:        %{obm_release}%{?dist}
Summary:        Open Business Management

Group:          Development/Languages
License:        AGPLv3
URL:            http://www.obm.org
Source0:        %{name}-%{version}.tar.gz
Source1:        %{name}-core.cron.d
Source2:        %{name}-admin.sh
Source3:        %{name}-config.sh
Source4:        %{name}-ldap.sh
Source5:        %{name}-mysql.sh
Source6:        %{name}-core.sh
Source7:        %{name}-cyrus.sh
Source8:        %{name}-postfix.sh
Source9:        %{name}-pgsql.sh
Source10:       %{name}-sysusers.sh
Source11:       %{name}-ui.sh
Source12:       pgadmin.sh
Source13:       myadmin.sh
Source14:       %{name}-services.cron.d
Source15:       %{name}-services.logrotate.d


BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildARch:      noarch
Requires:       %{name}-config = %{version}-%{release}
Requires:       %{name}-DataBase = %{version}-%{release} 
Requires:       %{name}-services = %{version}-%{release}
Requires:       %{name}-ldap = %{version}-%{release}
Requires:       %{name}-postfix = %{version}-%{release}
Requires:       %{name}-cyrus = %{version}-%{release}
Requires:       %{name}-ui = %{version}-%{release}
Requires:       %{name}-core = %{version}-%{release}
Requires:       %{name}-support = %{version}-%{release}
Requires:       %{name}-ca
Requires:       %{name}-cert
Requires:       %{name}-solr

%package        full
Summary:        Open Business Management, a groupware solution (metapackage)
Group:          Development/Tools
Requires:       %{name} = %{version}-%{release}
Requires:       %{name}-sync
Requires:       %{name}-imap-archive = %{version}-%{release}
Requires:       %{name}-provisioning = %{version}-%{release}

%description    full
This package is a metapackage that, when installed, guarantees that you have
all the different components of OBM installed. Removing this package won't
remove OBM from your system.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%description
This package is a metapackage that, when installed, guarantees that you have
the components necessary for the OBM web interface installed. Removing this
package won't remove OBM from your system.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.


%package	config
Summary:	configuration system for Open Business Management
Group:		Development/Tools

%description	config
This package generates the main OBM configuration file.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package        core
Summary:        web interface for Open Business Management
Group:          Development/Tools
Requires:       %{name}-config = %{version}-%{release}
Requires(post):        vixie-cron
Requires:       php >= 5.2, php-xml, php-mysql, php-gd, php-cli, php-pgsql, php-ldap, php-pecl-apc, php-pecl-imagick
Conflicts:	opush < 3

%description    core
This package contains the web interface for OBM. It has no dependency on the
other services needed by OBM, such as obm-storage.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	MySQL
Summary:	MySQL backend for OBM
Group:		Development/Tools

Requires:	%{name}-config = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}
Requires:	obm-locator
Requires:	mysql-server >= 5.0
Conflicts:	%{name}-PostgreSQL
Provides:	%{name}-DataBase = %{version}-%{release}

%description	MySQL
This package is the MySQL database backend for OBM.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	PostgreSQL
Summary:	PostgreSQL backend for OBM
Group:		Development/Tools

Requires:       postgresql-server >= 8.3
Requires:       %{name}-core = %{version}-%{release}
Requires:       %{name}-config = %{version}-%{release}
Requires:       obm-locator
Conflicts:      %{name}-MySQL
Provides:       %{name}-DataBase = %{version}-%{release}
#Introduced in 2.4.0
Provides:       %{name}-PostgreSQL91 = %{version}-%{release}
Obsoletes:      %{name}-PostgreSQL91 < %{version}
Provides:       %{name}-PostgreSQL83 = %{version}-%{release}
Obsoletes:      %{name}-PostgreSQL83 < %{version}

%description	PostgreSQL
This package contains PostgreSQL schemas and configuration
files for %{name} package.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.


%package        ui
Summary:        web interface configuration for OBM
Group:          Development/Tools

Requires:       %{name}-core = %{version}-%{release}
Requires: 	php >= 5.2, php-xml, php-mysql, php-gd, php-cli, php-pgsql, php-ldap, php-mbstring, php-imap, curl >= 7.20
Requires(pre):  httpd

%description    ui
This package configures Apache to serve OBM components (obm-ui, obm-storage, opush).

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	services
Summary:	integration of OBM with LDAP/Postfix/Cyrus
Group:		Development/Tools

Requires:	%{name}-config = %{version}-%{release}
Requires:	perl-OBM = %{version}-%{release}
Requires:	perl-DBD-MySQL 
Requires:	perl-DBD-Pg
Requires:	perl-LDAP
Requires:	perl-XML-Simple
Requires:	perl-core
Requires:	cyrus-sasl-plain

%description	services
This package is responsible for propagating changes in OBM to third-party
software used by OBM, namely Open LDAP, Postfix and Cyrus.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	-n perl-OBM
Summary:	library for the integration of OBM with LDAP/Postfix/Cyrus
Group:		Development/Libraries
License:        GPL+ or Artistic
#Requires:       %{name} = %{version}-%{release}


BuildARch:      noarch
BuildRequires:	perl(ExtUtils::MakeMaker)
%{?perl_module_compat:Requires: perl(:MODULE_COMPAT_%{perl_module_compat})}
%{!?perl_module_compat:Requires: perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))}
Requires:	perl-Class-Singleton
Requires:       perl-Digest-SHA

%description	-n perl-OBM
This package is responsible for propagating changes in OBM to third-party
software used by OBM, namely Open LDAP, Postfix and Cyrus.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	ldap
Summary:	LDAP configuration for OBM
Group:		Development/Tools

Requires:	%{name}-config = %{version}-%{release}
Requires:	openldap-servers
Requires:	openldap-clients

%description	ldap
This package is responsible for propagating changes in OBM to third-party
software used by OBM, namely Open LDAP, Postfix and Cyrus.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	postfix
Summary:        Postfix configuration for OBM
Group:          Development/Tools

Requires:       %{name}-config = %{version}-%{release}
Requires(post): chkconfig
Requires(post): obm-satellite
Conflicts:	sendmail
Requires:	postfix
Requires:	cyrus-sasl
Requires:	cyrus-sasl-plain

%description    postfix
This package configures Postfix for OBM.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package	cyrus
Summary:        Cyrus/SASL configuration for OBM
Group:          Development/Tools

Requires:       %{name}-config = %{version}-%{release}
Requires:	cyrus-imapd
Requires:	cyrus-imapd-utils
Requires(post): cyrus-sasl
Requires(post): cyrus-sasl-plain
Requires(post): obm-satellite
Requires(post): openldap-clients

%description    cyrus
This package configures Cyrus/SASL for OBM.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package        support
Summary:        OBM support utilities (metapackage)
Group:          Development/Tools

Requires:       vim-enhanced
Requires:       less
Requires:       telnet
Requires:       binutils
Requires:       bind-utils
Requires:       lsof
Requires:       strace
Requires:       tcpdump
Requires:       lynx
Requires:       mutt
Requires:       screen
Requires:       openldap-clients

%description    support
This package pulls in various tools (vim, telnet...) useful for OBM
administrators.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%prep
%setup -q -n %{name}-%{version} 

# Add default config
mv conf/obm_conf.inc.sample conf/obm_conf.inc
mv conf/obm_conf.ini.sample conf/obm_conf.ini

# Fix encoding, excluded sql schemas for now to avoid trouble interact
#for file in `ls scripts/2.1/*.txt` `ls doc/*/*/*.txt`; do
#	iconv -f ISO88591 -t UTF-8 $file -o file.tmp
#	mv file.tmp $file
#done

%build
cd auto/libperl-OBM
# Pass perl_vendorlib explicitly, this allows us to redefine it when we're not
# building on RedHat/CentOS
perl Makefile.PL INSTALLDIRS=vendor INSTALLVENDORLIB=%{perl_vendorlib}
cd -

%install
#ALL
rm -rf $RPM_BUILD_ROOT

#libperl stuff
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}
cd auto/libperl-OBM
make install PERL_INSTALL_ROOT=$RPM_BUILD_ROOT
cd -

#Remove unneeded stuff
find $RPM_BUILD_ROOT -type f -name .packlist -exec rm -f {} ';'

#obm-config
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE3} $RPM_BUILD_ROOT%{_bindir}/%{name}-config
install -p -m 755 %{SOURCE10} $RPM_BUILD_ROOT%{_bindir}/%{name}-sysusers
install -p -m 755 %{SOURCE2} $RPM_BUILD_ROOT%{_bindir}/%{name}-admin
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}
cp -a conf/* $RPM_BUILD_ROOT%{_sysconfdir}/%{name}

#ghost stuff
touch $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/%{name}-rpm.conf


#obm-core
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/modules
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/themes/images
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/backup
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/0
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/1
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/2
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/3
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/4
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/5
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/6
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/7
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/8
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/9
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}

cp -apR contrib cron obminclude php resources tests scripts locale views app lib \
	$RPM_BUILD_ROOT%{_datadir}/%{name}
cp conf/obm_conf.inc $RPM_BUILD_ROOT%{_sysconfdir}/%{name}
cp conf/webmail.inc.php.sample $RPM_BUILD_ROOT%{_sysconfdir}/%{name}
# Fix permissions
chmod 0664 \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/admin_code/admin_code_index.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/admin_lang/admin_lang_index.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/cron/Logger.class.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/time/time_index.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/time/*.inc \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/resources/*/*/*/*.css \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/resources/*/*/*.inc \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/%{name}include/*/of_session.inc

ln -s %{_datadir}/%{name}/resources $RPM_BUILD_ROOT%{_datadir}/%{name}/php/images
ln -s %{_datadir}/%{name}/php/webmail/ $RPM_BUILD_ROOT%{_datadir}/%{name}/scripts/2.4/roundcube

cd $RPM_BUILD_ROOT%{_sysconfdir}
ln -s %{_sysconfdir}/%{name} $RPM_BUILD_ROOT%{_datadir}/%{name}/conf
cd -
mkdir $RPM_BUILD_ROOT%{_sysconfdir}/cron.d/
install -p -m 644 %{SOURCE1} $RPM_BUILD_ROOT%{_sysconfdir}/cron.d/obm-core

cp conf/modules/module.inc $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/modules
cp conf/themes/login.html.sample $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/themes
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE6} $RPM_BUILD_ROOT%{_bindir}/obm-core

#obm-ui
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}-ui
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/httpd/conf.d
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE11} $RPM_BUILD_ROOT%{_bindir}/%{name}-ui
install -p -m 644 doc/conf/apache-virtualhost_obm.conf.sample $RPM_BUILD_ROOT%{_sysconfdir}/httpd/conf.d/%{name}.conf
install -p -m 644 utils/timezone-generator.php $RPM_BUILD_ROOT%{_datadir}/%{name}-ui
install -p -m 755 utils/purge_contacts.py $RPM_BUILD_ROOT%{_datadir}/%{name}-ui

# obm-services
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}-services/updates
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/%{name}-services
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d
#chmod g+s $RPM_BUILD_ROOT%{_localstatedir}/log/%{name}
cp -apR auto/testObmSatellite.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/cyrusGetQuotaUse.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/updateCyrusAcl.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/update.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/updateSieve.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/changePasswd.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/ldapContacts.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR scripts/2.3/update-2.2-2.3.ldap.pl $RPM_BUILD_ROOT%{_datadir}/%{name}-services/updates
install -p -m 640 %{SOURCE15} $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/%{name}-services
install -p -m 0644 %{SOURCE14} $RPM_BUILD_ROOT%{_sysconfdir}/cron.d/%{name}-services

# obm-ldap
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/openldap/schema
install -p -m 644 doc/conf/ldap_samba.schema.3.0.24.sample $RPM_BUILD_ROOT%{_sysconfdir}/openldap/schema/samba.schema
install -p -m 644 doc/conf/ldap_obm.schema.sample $RPM_BUILD_ROOT%{_sysconfdir}/openldap/schema/obm.schema
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE4} $RPM_BUILD_ROOT%{_bindir}/obm-ldap

#obm-postfix
mkdir -p $RPM_BUILD_ROOT%{_bindir}

install -p -m 755 %{SOURCE8} $RPM_BUILD_ROOT%{_bindir}/obm-postfix

# obm-mysql
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}-MySQL-%{version}
install -p -m 755 %{SOURCE5} $RPM_BUILD_ROOT%{_bindir}/%{name}-mysql
install -p -m 755 %{SOURCE13} $RPM_BUILD_ROOT%{_bindir}/myadmin.lib

# obm-pgsql
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}-PostgreSQL-%{version}
install -p -m 755 %{SOURCE9} $RPM_BUILD_ROOT%{_bindir}/%{name}-pgsql
install -p -m 755 %{SOURCE12} $RPM_BUILD_ROOT%{_bindir}/pgadmin.lib

# obm-cyrus
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE7} $RPM_BUILD_ROOT%{_bindir}/%{name}-cyrus

# Remove version-controle-internal file ---> useless
find $RPM_BUILD_ROOT -name '.cvsignore' -exec rm -rf {} ';'

%clean
rm -rf $RPM_BUILD_ROOT

%post		-n %{name}-ui
if [ "$1" = "1" ] ; then
  chkconfig httpd on &>/dev/null
  if [ -f %{_sbindir}/setsebool ] ; then
    %{_sbindir}/setsebool httpd_can_network_connect=1 &>/dev/null
  fi

  echo "Generating timezone definitions..."
  php /usr/share/obm-ui/timezone-generator.php -d /usr/share/obm/resources/js/bin/timezone -c
else
  touch ~/obm-generate-tz
fi


%posttrans  -n %{name}-ui
if [ -e ~/obm-generate-tz ]; then
  rm -f ~/obm-generate-tz
  
  echo "Generating timezone definitions..."
  php /usr/share/obm-ui/timezone-generator.php -d /usr/share/obm/resources/js/bin/timezone -c
fi

%files
%defattr(-,root,root,-)

%files	-n %{name}-core
%{_bindir}/%{name}-core
%defattr(-,root,root,-)
%doc doc/* LEGAL_NOTICE THIRD_PARTY README 
%attr(-,apache,apache) %dir %{_datadir}/%{name}
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/backup
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/0
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/1
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/2
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/3
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/4
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/5
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/6
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/7
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/8
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/9
%{_datadir}/%{name}/conf
%{_datadir}/%{name}/contrib
%{_datadir}/%{name}/cron
%{_datadir}/%{name}/obminclude
%{_datadir}/%{name}/php
%{_datadir}/%{name}/resources
%{_datadir}/%{name}/tests
%{_datadir}/%{name}/scripts
%{_datadir}/%{name}/views
%{_datadir}/%{name}/app
%{_datadir}/%{name}/lib
%{_datadir}/%{name}/locale
%{_sysconfdir}/%{name}/hooks
%{_sysconfdir}/%{name}/themes
%{_sysconfdir}/%{name}/modules
%{_sysconfdir}/%{name}/webmail.inc.php.sample
%{_sysconfdir}/cron.d/obm-core

%attr(770,-,apache) %dir %{_datadir}/%{name}/php/webmail/temp
%attr(770,-,apache) %dir %{_datadir}/%{name}/php/webmail/logs

%files		-n perl-OBM
%defattr(-,root,root,-)
%{perl_vendorlib}/OBM
%{_mandir}/man3/*.3pm*

%files		-n %{name}-ui
%config(noreplace) %{_sysconfdir}/httpd/conf.d/*.conf
%{_bindir}/%{name}-ui
%{_datadir}/%{name}-ui

%files          -n %{name}-full
#no files in this package

%files		-n %{name}-config
%defattr(-,root,root,-)
%{_bindir}/%{name}-config
%{_bindir}/%{name}-admin
%{_bindir}/%{name}-sysusers
%{_bindir}/pgadmin.lib
%dir %{_sysconfdir}/%{name}
%ghost %{_sysconfdir}/%{name}/%{name}-rpm.conf
%verify(not size,not md5) %config(noreplace) %{_sysconfdir}/%{name}/%{name}_conf.ini
%verify(not size,not md5) %config(noreplace) %{_sysconfdir}/%{name}/%{name}_conf.inc
%verify(not size,not md5) %config(noreplace) %{_sysconfdir}/%{name}/automateLdapMapping.xml

%files		-n %{name}-MySQL
%defattr(-,root,root,-)
%{_bindir}/obm-mysql
%{_bindir}/myadmin.lib

%post           -n %{name}-MySQL
if [ "$1" = "2" ]; then
        echo "Finish upgrade Database, check /usr/share/obm/script upgrade script"
fi

%files          -n %{name}-PostgreSQL
%defattr(-,root,root,-)
%{_bindir}/obm-pgsql

%post           -n %{name}-PostgreSQL
if [ "$1" = "2" ]; then
        echo "Finish upgrade Database, check /usr/share/obm/script upgrade script"
fi

%files		-n %{name}-services
%config %{_sysconfdir}/cron.d/%{name}-services
%{_datadir}/%{name}/auto
%{_datadir}/%{name}-services/updates
%config(noreplace) %{_sysconfdir}/logrotate.d/%{name}-services
%attr(2750, apache, apache) %{_localstatedir}/log/%{name}-services


%post           -n %{name}-services
if [ "$1" = "2" ]; then
  /usr/share/obm/auto/update.pl --global --domain-global
  echo "!!!!! Be careful !!!!! If you upgrade 2.2.x to 2.3.x please run update script in /usr/share/obm-services/updates/ "
fi



%files 		-n %{name}-ldap
%doc doc/conf/ldap_slapd.conf.sample
%{_sysconfdir}/openldap/schema/obm.schema
%{_sysconfdir}/openldap/schema/samba.schema
%{_bindir}/%{name}-ldap

%files		-n %{name}-postfix
%{_bindir}/%{name}-postfix
%doc 	doc/conf/postfix_main.cf_-_SMTPin.2.3.x.sample
%doc 	doc/conf/postfix_main.cf_-_SMTPout.2.3.x.sample


%post           -n %{name}-postfix
if [ "$1" = "1" ]; then
  echo -n "[obm-postfix] activating obm-satellite postfix module..."
  /usr/sbin/osenmod postfixSmtpInMaps
  /usr/sbin/osenmod postfixAccess
  if [ -e /etc/obm/certs/obm_cert.pem ] ; then
    /etc/init.d/obm-satellite start
  fi
fi
%{_sbindir}/alternatives --set mta /usr/sbin/sendmail.postfix || :

if [ "$1" = "2" ]; then
  /etc/init.d/obm-satellite restart
fi



%postun         -n %{name}-postfix
if [ "$1" = "0" ]; then
  echo -n "[obm-postfix] removing obm-satellite postfix module..."
  if [ -e /usr/sbin/osdismod ]; then
    /usr/sbin/osdismod postfixSmtpInMaps
    /usr/sbin/osdismod postfixAccess
  fi
fi


%files		-n %{name}-cyrus
%{_bindir}/%{name}-cyrus
%doc 	doc/conf/cyrus_cyrus.conf.sample
%doc 	doc/conf/cyrus_imapd.conf.sample
%doc 	doc/conf/cyrus_saslauthd.conf.sample
%doc 	doc/conf/cyrus_saslauthd.sample

%post           -n %{name}-cyrus
#obm-cyrus install
if [ "$1" = "1" ] ; then
  echo -n "[obm-cyrus] activating obm-satellite cyrus module..."
  /usr/sbin/osenmod cyrusPartition
  /usr/sbin/osenmod backupEntity
  #We need a certificate before starting
  if [ -e /etc/obm/certs/obm_cert.pem ] ; then
    echo -n "[obm-cyrus] starting obm-satellite cyrus module..."
    /etc/init.d/obm-satellite start
  fi
fi

#obm-cyrus upgrade from 2.3
if [ "$1" = "2" ]; then
  imapd_file="/etc/imapd.conf"
  saslauthd_file="/etc/saslauthd.conf"
  SINGLE_NAME_SPACE="OK"
  grep -i '^virtdomains.*userid' ${imapd_file} && SINGLE_NAME_SPACE="NOK"
  if [ $SINGLE_NAME_SPACE = 'OK' ]; then
    echo -n "[obm-cyrus] update imapd.conf ... "

    cp ${imapd_file} ${imapd_file}_BEFORE_OBM_2.3_UPDATE
    LDAPSERVER=`cat /etc/obm/obm_conf.ini | grep -i "ldapServer" | cut -d'=' -f2 | sed -e "s/ //g"|sed -e "s#ldap://##"`

    DEFAULT_DOMAIN=`ldapsearch -x -h ${LDAPSERVER} '(dc=*)' | grep dn | grep -v 'dn: dc=local' | sed -e 's/dn: //' | sed -e 's/\,dc=local//' | cut -d"=" -f2`

    sed -i -e 's/\(^virtdomains.*\)/###COMMENT_BY_UPGRADE##\1/' ${imapd_file}

    echo "virtdomains: userid" >> ${imapd_file}
    echo "defaultdomain: ${DEFAULT_DOMAIN}" >> ${imapd_file}
    echo "DONE."

    echo -n "[obm-cyrus] update saslauthd.conf ... "
    cp ${saslauthd_file} ${saslauthd_file}_BEFORE_OBM_2.3_UPDATE
    sed -i -e 's/\(^ldap_filter.*\)/###COMMENT_BY_UPGRADE##\1/' $saslauthd_file
    ldap_filter="(|(&(|(mailBox=%U@%d)(mailBox=%U@<singleDomainName>))(objectClass=obmUser)(mailAccess=PERMIT))(&(uid=%U)(cn=Administrator Cyrus*)(objectClass=posixAccount)))"
    echo "# Use this filter when migrating from an SingleNameSpace installation" >> ${saslauthd_file}
    echo "# IMPORTANT : see 'defaultdomain' into '/etc/imapd.conf' too" >> ${saslauthd_file}
    echo "# ie: migrating a single namespace installation, domain : foo.com" >> ${saslauthd_file}
    echo "#     replace '<singleDomainName>' by 'foo.com'" >> ${saslauthd_file}
    echo "ldap_filter: ${ldap_filter}" >> ${saslauthd_file}
    echo "DONE."
  fi
  echo -n "[obm-cyrus] restarting obm-satellite cyrus module..."
  /etc/init.d/obm-satellite restart
fi



%postun         -n %{name}-cyrus
if [ "$1" = "0" ]; then
  echo -n "[obm-cyrus] removing obm-satellite cyrus module..."
  if [ -e /usr/sbin/osdismod ]; then
    /usr/sbin/osdismod cyrusPartition
    /usr/sbin/osdismod backupEntity
  fi
fi

%files          -n %{name}-support
#empty package :(

%changelog
* Wed Jul 08 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.6-0.rc3
- New upstream release.
* Tue Jul 07 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.6-0.rc2
- New upstream release.
* Mon Jul 06 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.6-0.rc1
- New upstream release.
* Wed May 27 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.5-1
- New upstream release.
* Thu May 21 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.5-0.rc4
- New upstream release.
* Wed May 20 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.5-0.rc3
- New upstream release.
* Wed May 13 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.5-0.rc2
- New upstream release.
* Tue May 12 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.5-0.rc1
- New upstream release.
* Mon Mar 30 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.4-1
- New upstream release.
* Thu Mar 26 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.4-0.rc1
- New upstream release.
* Mon Mar 02 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.3-
- New upstream release.
* Wed Feb 25 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.3-0.rc2
- New upstream release.
* Thu Feb 19 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.3-0.rc1
- New upstream release.
* Tue Nov 25 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.0-0.rc1
- New upstream release.
* Wed Nov 19 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-3.1.0-0.alpha0
- New upstream release.
* Tue Jul 22 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-3.0.0-1
- New upstream release.
* Tue Jun 10 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-3.0.0-0.rc2
- New upstream release.
* Thu May 22 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-3.0.0-0.rc1
- New upstream release.
* Tue Apr 30 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-2.5.0-
- New upstream release.
* Mon Apr 29 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-2.5.0-0.rc3
- New upstream release.
* Fri Apr 26 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-2.5.0-0.rc2
- New upstream release.
* Tue Apr 23 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-2.5.0-0.rc1
- New upstream release.
* Fri Jan 18 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-2.5.0.0-0.alpha0
- New upstream release.
* Thu Jan 17 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.beta7
- New upstream release.
* Wed Dec 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.beta6
- New upstream release.
* Tue Dec 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.beta5
- New upstream release.
* Mon Dec 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.beta4
- New upstream release.
* Tue Nov 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.beta3
- New upstream release.
* Wed Nov 07 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.beta2
- New upstream release.
* Mon Nov 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.beta1
- New upstream release.
* Tue Oct 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.alpha3
- New upstream release.
* Mon Oct 15 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.alpha2
- New upstream release.
* Wed Sep 26 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.2.0-0.alpha1
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1.1-0.rc2
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1.1-0.rc1
- New upstream release.
* Wed Sep 12 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1.1-0.beta1
- New upstream release.
* Mon Sep 03 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1.0-1
- New upstream release.
* Fri Aug 31 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-
- New upstream release.
* Thu Jul 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-beta2
- New upstream release.
* Mon Jul 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-beta2
- New upstream release.
* Fri Jul 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-beta1
- New upstream release.
* Tue Jun 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha11
- New upstream release.
* Mon May 21 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha10
- New upstream release.
* Fri Apr 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha9
- New upstream release.
* Thu Apr 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha8
- New upstream release.
* Tue Apr 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha7
- New upstream release.
* Thu Apr 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha6
- New upstream release.
* Fri Mar 02 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha5
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha4
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha3
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha2
- New upstream release.
* Fri Jan 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.1-alpha1
- New upstream release.
* Wed Jan 04 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.0.0-rc16
- New upstream release.
* Thu Dec 08 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.0.0-rc15
- New upstream release.
* Wed Nov 30 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-2.4.0.0-rc14
- New upstream release.
* Tue Jul 21 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.7-2
- remove php-magik require
* Sun Apr 22 2009 Ronan Lanore <ronan.lanore[at]obm.org> - 2.2.1-2
- fix update conffile

* Sun Apr 19 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.1-1
- New upstream version and first stable rpm release

* Wed Feb 25 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.0-3
- bump version

* Mon Feb 23 2009 Ronan Lanore <ronan.lanore[at]obm.org>
+ obm-2.2.0-2
- Add Dep perl-Class-Singleton to obm-services

* Fri Feb 20 2009 Ronan Lanore <ronan.lanore[at]obm.org> - 2.2.0-2
- Change dep php to php >= 5.2
- Add dep perl-DBD-Pg for obm-service
- Add dep php-ldap in obm-core and obm-ui
- Add dep php-pgsql for obm-ui and obm-core

* Tue Dec 30 2008 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.0-1
- Bump version.
* Thu Jun 19 2008 Sylvain Garcia <sylvain.garcia[at]aliasource.fr> - 2.1.10-1
- New upstream.
- add obm-ldap.
* Mon Apr 21 2008 Xavier Lamien <lxtnow[at]gmail.com> - 2.1.9-1
- Initial RPM Release.
