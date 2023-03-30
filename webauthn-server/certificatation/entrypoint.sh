#!/bin/sh

CONFIG_DIR=./conf
CERT_DIR=./cert

CA_CONFIG_FILE=ca.config
CSR_CONFIG_FILE=csr.config
EXT_CONFIG_FILE=cert.ext
PW_CA_FILE=pw_ca.txt
PW_CERT_FILE=pw_cert.txt
CA_KEY_FILE=ca.key
CA_CERT_FILE=ca.pem
PRIVATE_KEY_FILE=private.key
CSR_FILE=request.csr
CERT_FILE=public.crt

mkdir -p $CERT_DIR

if [ -f "$CERT_DIR/$CA_CERT_FILE" ]; then
  echo "CA exists, will continue, did NOT check for validity"
else
  echo "Generating CA, watch out for collateral damage!"
  # Generate Passwords
  openssl rand -base64 32 > $CERT_DIR/$PW_CA_FILE
  openssl genrsa -des3 -passout file:$CERT_DIR/$PW_CA_FILE -out $CERT_DIR/$CA_KEY_FILE 2048
  openssl req -x509 -batch -new -nodes -passin file:$CERT_DIR/$PW_CA_FILE -key $CERT_DIR/$CA_KEY_FILE -config $CONFIG_DIR/$CA_CONFIG_FILE -sha256 -days 1825 -out $CERT_DIR/$CA_CERT_FILE
  # EXTERNAL COMMAND
#  sudo security add-trusted-cert -d -r trustRoot -k "/Library/Keychains/System.keychain" ${CA_CERT_FILE}
fi

if [ -f "$CERT_DIR/$CERT_FILE" ]; then
  echo "Cert exists, I will not be bothered, did NOT check for validity"
else
  echo "Generating Cert, stand back please!"

  openssl rand -base64 32 > $CERT_DIR/$PW_CERT_FILE
  openssl genrsa -passout file:$CERT_DIR/$PW_CERT_FILE -out $CERT_DIR/$PRIVATE_KEY_FILE 2048
  openssl req -new -batch -key $CERT_DIR/$PRIVATE_KEY_FILE -passin file:$CERT_DIR/$PW_CERT_FILE -config $CONFIG_DIR/$CSR_CONFIG_FILE -out $CERT_DIR/$CSR_FILE
  openssl x509 -req -in $CERT_DIR/$CSR_FILE -CA $CERT_DIR/$CA_CERT_FILE -CAkey $CERT_DIR/$CA_KEY_FILE -passin file:$CERT_DIR/$PW_CA_FILE -CAcreateserial -out $CERT_DIR/$CERT_FILE -days 365 -sha256 -extfile $CONFIG_DIR/$EXT_CONFIG_FILE
fi
