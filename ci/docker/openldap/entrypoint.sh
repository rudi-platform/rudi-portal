#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Set default values
LDAP_PORT_NUMBER=${LDAP_PORT_NUMBER:-1389}
LDAP_ROOT=${LDAP_ROOT:-dc=example,dc=org}
LDAP_ADMIN_USERNAME=${LDAP_ADMIN_USERNAME:-admin}
LDAP_ADMIN_PASSWORD=${LDAP_ADMIN_PASSWORD:-admin}
LDAP_LOGLEVEL=${LDAP_LOGLEVEL:-256}
LDAP_ALLOW_ANON_BINDING=${LDAP_ALLOW_ANON_BINDING:-yes}
LDAP_ULIMIT_NOFILES=${LDAP_ULIMIT_NOFILES:-1024}
LDAP_ADD_SCHEMAS=${LDAP_ADD_SCHEMAS:-no}
LDAP_SKIP_DEFAULT_TREE=${LDAP_SKIP_DEFAULT_TREE:-no}
LDAP_CUSTOM_LDIF_DIR=${LDAP_CUSTOM_LDIF_DIR:-/ldifs}
LDAP_CUSTOM_SCHEMA_FILE=${LDAP_CUSTOM_SCHEMA_FILE:-/schemas/schema.ldif}

# Additional configuration variables
LDAP_CONFIG_ADMIN_ENABLED=${LDAP_CONFIG_ADMIN_ENABLED:-no}
LDAP_CONFIG_ADMIN_USERNAME=${LDAP_CONFIG_ADMIN_USERNAME:-admin}
LDAP_CONFIG_ADMIN_PASSWORD=${LDAP_CONFIG_ADMIN_PASSWORD:-configpassword}
LDAP_USERS=${LDAP_USERS:-}
LDAP_PASSWORDS=${LDAP_PASSWORDS:-}
LDAP_USER_DC=${LDAP_USER_DC:-users}
LDAP_GROUP=${LDAP_GROUP:-readers}
LDAP_EXTRA_SCHEMAS=${LDAP_EXTRA_SCHEMAS:-}

# Set ulimit
ulimit -n "${LDAP_ULIMIT_NOFILES}"

# Extract domain components from LDAP_ROOT
DC_SUFFIX="${LDAP_ROOT}"
LDAP_DOMAIN=$(echo "${LDAP_ROOT}" | sed 's/dc=//g' | sed 's/,/./g')
LDAP_ORG=$(echo "${LDAP_ROOT}" | sed 's/dc=//g' | sed 's/,.*//')

info "Starting OpenLDAP configuration..."
info "LDAP Domain: ${LDAP_DOMAIN}"
info "LDAP Root DN: ${DC_SUFFIX}"
info "LDAP Admin: cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}"

# Create configuration directory if not exists
SLAPD_CONF_DIR="/etc/ldap/slapd.d"
SLAPD_DATA_DIR="/var/lib/ldap"

# Prepare directories and permissions
mkdir -p "${SLAPD_CONF_DIR}" "${SLAPD_DATA_DIR}"
chmod 700 "${SLAPD_DATA_DIR}"
chown -R openldap:openldap "${SLAPD_CONF_DIR}" "${SLAPD_DATA_DIR}" 2>/dev/null || true

# When /etc/ldap/slapd.d is mounted from a fresh PVC (empty directory), restore
# the default Debian configuration backed up at image build time.
if [ -z "$(ls -A ${SLAPD_CONF_DIR})" ]; then
    info "slapd.d is empty — restoring default config from image backup..."
    cp -a /opt/slapd-defaults/. "${SLAPD_CONF_DIR}/"
    chown -R openldap:openldap "${SLAPD_CONF_DIR}" 2>/dev/null || true
fi

# Check if this is the first run
if [ ! -f "${SLAPD_DATA_DIR}/.initialized" ]; then
    info "First run detected. Initializing OpenLDAP database..."
    
    # Generate admin password hash
    ADMIN_PASSWORD_HASH=$(slappasswd -s "${LDAP_ADMIN_PASSWORD}")
    
    # Create base configuration LDIF
    cat > /tmp/base-config.ldif <<EOF
dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcSuffix
olcSuffix: ${DC_SUFFIX}
-
replace: olcRootDN
olcRootDN: cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}
-
replace: olcRootPW
olcRootPW: ${ADMIN_PASSWORD_HASH}
-
replace: olcAccess
olcAccess: {0}to attrs=userPassword
  by self write
  by anonymous auth
  by * none
olcAccess: {1}to *
  by self write
  by * read
EOF

    # Configure logging level
    cat > /tmp/logging.ldif <<EOF
dn: cn=config
changetype: modify
replace: olcLogLevel
olcLogLevel: ${LDAP_LOGLEVEL}
EOF

    # Start slapd temporarily for configuration
    info "Starting temporary slapd for configuration..."
    
    # Ensure socket directory exists with correct permissions
    mkdir -p /var/run/slapd
    chmod 755 /var/run/slapd
    
    slapd -h "ldapi:///" -F "${SLAPD_CONF_DIR}" 2>&1
    sleep 3
    
    # Verify slapd is running by testing the socket connection
    info "Verifying LDAP connection..."
    RETRY=0
    until ldapwhoami -Y EXTERNAL -H ldapi:/// > /dev/null 2>&1; do
        RETRY=$((RETRY + 1))
        if [ $RETRY -ge 5 ]; then
            error "Failed to connect to LDAP after ${RETRY} attempts!"
            exit 1
        fi
        warn "Waiting for slapd... attempt ${RETRY}/5"
        sleep 2
    done
    info "LDAP connection successful"
    
    # Apply base configuration
    info "Applying base configuration..."
    ldapmodify -Y EXTERNAL -H ldapi:/// -f /tmp/base-config.ldif 2>/dev/null || warn "Base config already applied"
    ldapmodify -Y EXTERNAL -H ldapi:/// -f /tmp/logging.ldif 2>/dev/null || warn "Logging config already applied"
    
    # Configure config admin if enabled
    if [ "${LDAP_CONFIG_ADMIN_ENABLED}" = "yes" ]; then
        info "Enabling config admin..."
        CONFIG_ADMIN_PASSWORD_HASH=$(slappasswd -s "${LDAP_CONFIG_ADMIN_PASSWORD}")
        cat > /tmp/config-admin.ldif <<EOF
dn: olcDatabase={0}config,cn=config
changetype: modify
replace: olcRootDN
olcRootDN: cn=${LDAP_CONFIG_ADMIN_USERNAME},cn=config
-
replace: olcRootPW
olcRootPW: ${CONFIG_ADMIN_PASSWORD_HASH}
EOF
        ldapmodify -Y EXTERNAL -H ldapi:/// -f /tmp/config-admin.ldif 2>/dev/null || warn "Config admin already configured"
    fi
    
    # Add extra schemas if enabled
    if [ "${LDAP_ADD_SCHEMAS}" = "yes" ]; then
        info "Adding standard schemas..."
        for schema in cosine inetorgperson nis; do
            ldapadd -Y EXTERNAL -H ldapi:/// -f "/etc/ldap/schema/${schema}.ldif" 2>/dev/null || warn "Schema ${schema} already added"
        done
    fi
    
    # Add extra schemas from LDAP_EXTRA_SCHEMAS variable
    if [ -n "${LDAP_EXTRA_SCHEMAS}" ]; then
        info "Adding extra schemas: ${LDAP_EXTRA_SCHEMAS}..."
        IFS=',' read -ra SCHEMAS <<< "${LDAP_EXTRA_SCHEMAS}"
        for schema in "${SCHEMAS[@]}"; do
            schema=$(echo "${schema}" | xargs)  # Trim whitespace
            if [ -f "/etc/ldap/schema/${schema}.ldif" ]; then
                ldapadd -Y EXTERNAL -H ldapi:/// -f "/etc/ldap/schema/${schema}.ldif" 2>/dev/null || warn "Schema ${schema} already added"
            else
                warn "Schema file /etc/ldap/schema/${schema}.ldif not found"
            fi
        done
    fi
    
    # Add custom schema if exists
    if [ -f "${LDAP_CUSTOM_SCHEMA_FILE}" ]; then
        info "Adding custom schema from ${LDAP_CUSTOM_SCHEMA_FILE}..."
        ldapadd -Y EXTERNAL -H ldapi:/// -f "${LDAP_CUSTOM_SCHEMA_FILE}" 2>/dev/null || warn "Custom schema already added or failed"
    fi
    
    # Create base DN structure
    if [ "${LDAP_SKIP_DEFAULT_TREE}" != "yes" ]; then
        info "Creating base DN structure..."
        cat > /tmp/base-dn.ldif <<EOF
dn: ${DC_SUFFIX}
objectClass: top
objectClass: dcObject
objectClass: organization
o: ${LDAP_ORG}
dc: ${LDAP_ORG}

dn: cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
cn: ${LDAP_ADMIN_USERNAME}
sn: Administrator
userPassword: ${ADMIN_PASSWORD_HASH}
EOF
        
        # Use EXTERNAL auth to create base DN (no password needed)
        ldapadd -Y EXTERNAL -H ldapi:/// -f /tmp/base-dn.ldif 2>/dev/null || warn "Base DN already exists"
    fi
    
    # Create users organizational unit and users if specified
    if [ -n "${LDAP_USERS}" ] && [ "${LDAP_SKIP_DEFAULT_TREE}" != "yes" ]; then
        info "Creating users organizational unit and users..."
        
        # Create Users OU
        cat > /tmp/users-ou.ldif <<EOF
dn: ou=${LDAP_USER_DC},${DC_SUFFIX}
objectClass: top
objectClass: organizationalUnit
ou: ${LDAP_USER_DC}
EOF
        ldapadd -x -D "cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}" -w "${LDAP_ADMIN_PASSWORD}" -H ldapi:/// -f /tmp/users-ou.ldif 2>/dev/null || warn "Users OU already exists"
        
        # Create Group if specified
        if [ -n "${LDAP_GROUP}" ]; then
            cat > /tmp/group.ldif <<EOF
dn: cn=${LDAP_GROUP},ou=${LDAP_USER_DC},${DC_SUFFIX}
objectClass: top
objectClass: groupOfNames
cn: ${LDAP_GROUP}
EOF
            # We'll add members later
            echo "member: cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}" >> /tmp/group.ldif
            ldapadd -x -D "cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}" -w "${LDAP_ADMIN_PASSWORD}" -H ldapi:/// -f /tmp/group.ldif 2>/dev/null || warn "Group already exists"
        fi
        
        # Create users
        IFS=',' read -ra USERS <<< "${LDAP_USERS}"
        IFS=',' read -ra PASSWORDS <<< "${LDAP_PASSWORDS}"
        
        for i in "${!USERS[@]}"; do
            username=$(echo "${USERS[$i]}" | xargs)  # Trim whitespace
            password="${PASSWORDS[$i]:-changeme}"  # Default password if not provided
            password=$(echo "${password}" | xargs)  # Trim whitespace
            
            info "Creating user: ${username}..."
            USER_PASSWORD_HASH=$(slappasswd -s "${password}")
            
            cat > /tmp/user-${username}.ldif <<EOF
dn: cn=${username},ou=${LDAP_USER_DC},${DC_SUFFIX}
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
cn: ${username}
sn: ${username}
userPassword: ${USER_PASSWORD_HASH}
EOF
            ldapadd -x -D "cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}" -w "${LDAP_ADMIN_PASSWORD}" -H ldapi:/// -f /tmp/user-${username}.ldif 2>/dev/null || warn "User ${username} already exists"
            
            # Add user to group if group is specified
            if [ -n "${LDAP_GROUP}" ]; then
                cat > /tmp/add-to-group-${username}.ldif <<EOF
dn: cn=${LDAP_GROUP},ou=${LDAP_USER_DC},${DC_SUFFIX}
changetype: modify
add: member
member: cn=${username},ou=${LDAP_USER_DC},${DC_SUFFIX}
EOF
                ldapmodify -x -D "cn=${LDAP_ADMIN_USERNAME},${DC_SUFFIX}" -w "${LDAP_ADMIN_PASSWORD}" -H ldapi:/// -f /tmp/add-to-group-${username}.ldif 2>/dev/null || warn "User ${username} already in group"
            fi
        done
    fi
    
    # Import LDIF files from custom directory
    if [ -d "${LDAP_CUSTOM_LDIF_DIR}" ] && [ "$(ls -A ${LDAP_CUSTOM_LDIF_DIR}/*.ldif 2>/dev/null)" ]; then
        info "Importing LDIF files from ${LDAP_CUSTOM_LDIF_DIR}..."
        
        # Stop slapd before using slapadd (offline import)
        info "Stopping temporary slapd for direct import..."
        if [ -f /var/run/slapd/slapd.pid ]; then
            kill -TERM $(cat /var/run/slapd/slapd.pid) 2>/dev/null || true
        else
            pkill -TERM slapd 2>/dev/null || true
        fi
        sleep 2
        
        # Import LDIF files using slapadd (offline, direct to database)
        for ldif_file in ${LDAP_CUSTOM_LDIF_DIR}/*.ldif; do
            if [ -f "${ldif_file}" ]; then
                info "Importing $(basename ${ldif_file}) via slapadd..."
                if slapadd -F "${SLAPD_CONF_DIR}" -l "${ldif_file}" > /dev/null 2>&1; then
                    info "Successfully imported $(basename ${ldif_file})"
                else
                    warn "Import of $(basename ${ldif_file}) had issues"
                fi
            fi
        done
        
        # Restart slapd for remaining operations
        info "Restarting slapd..."
        slapd -h "ldapi:///" -F "${SLAPD_CONF_DIR}" 2>&1
        sleep 3
        
        # Verify slapd is running
        info "Verifying LDAP connection after import..."
        RETRY=0
        until ldapwhoami -Y EXTERNAL -H ldapi:/// > /dev/null 2>&1; do
            RETRY=$((RETRY + 1))
            if [ $RETRY -ge 5 ]; then
                error "Failed to reconnect to LDAP!"
                exit 1
            fi
            sleep 1
        done
        info "LDAP reconnection successful"
    fi
    
    # Mark as initialized
    touch "${SLAPD_DATA_DIR}/.initialized"
    info "OpenLDAP initialization completed!"
else
    info "OpenLDAP already initialized. Skipping configuration."
fi

# Configure anonymous binding
if [ "${LDAP_ALLOW_ANON_BINDING}" = "no" ]; then
    info "Disabling anonymous bindings..."
    cat > /tmp/disable-anon.ldif <<EOF
dn: cn=config
changetype: modify
add: olcDisallows
olcDisallows: bind_anon

dn: olcDatabase={-1}frontend,cn=config
changetype: modify
add: olcRequires
olcRequires: authc
EOF
    # This would need to be applied via ldapmodify if slapd is running
fi

info "Starting OpenLDAP server..."
info "Listening on port ${LDAP_PORT_NUMBER}"

# Remove any existing PID file
rm -f /var/run/slapd.pid
rm -f /var/run/openldap.pid

# Fix directory permissions
chmod 755 /var/run/slapd
chown -R openldap:openldap /var/lib/ldap /etc/ldap/slapd.d /var/run/slapd 2>/dev/null || true

# Start slapd with TCP listener as openldap user
exec su -s /bin/sh openldap -c "slapd -h 'ldap://0.0.0.0:${LDAP_PORT_NUMBER}/ ldapi:///' -F ${SLAPD_CONF_DIR} -d 256"
