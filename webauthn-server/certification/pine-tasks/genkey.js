const { run, color, shell, log } = require('@pinefile/pine');
const fs = require('fs');
const path = require('path');
const uuid = require('uuid');

const CERT_DIR = path.join(process.cwd(), 'certs');
const GENERATED_DIR = path.join(CERT_DIR, 'generated');
const CONFIG_DIR = path.join(CERT_DIR, 'config');

const CA_CONFIG_FILE = path.join(CONFIG_DIR, 'ca.config');
const CSR_CONFIG_FILE = path.join(CONFIG_DIR, 'csr.config');
const EXT_CONFIG_FILE = path.join(CONFIG_DIR, 'cert.ext');
const PW_CA_FILE = path.join(CONFIG_DIR, 'pw_ca.txt');
const PW_CERT_FILE = path.join(CONFIG_DIR, 'pw_cert.txt');

const CA_KEY_FILE = path.join(GENERATED_DIR, 'ca.key');
const CA_CERT_FILE = path.join(GENERATED_DIR, 'ca.pem');
const PRIVATE_KEY_FILE = path.join(GENERATED_DIR, 'private.key');
const CSR_FILE = path.join(GENERATED_DIR, 'request.csr');
const CERT_FILE = path.join(GENERATED_DIR, 'public.crt');

const cmd = async (command, ignoreLog) => {
  if (!ignoreLog) {
    log.info(color.yellow(command));
  }
  return shell(command);
};

module.exports = {
  // default is build
  default: async () => {
    await run('pine genkey:ca');
    await run('pine genkey:cert');
  },
  ca: async () => {
    await run('pine genkey:ensure:generated');
    await run('pine genkey:pw');
    await run('pine genkey:ca:key');
    await run('pine genkey:ca:cert');
    await run('pine genkey:ca:install');
  },
  cert: async () => {
    await run('pine genkey:ensure:generated');
    await run('pine genkey:pw');
    await run('pine genkey:cert:key');
    await run('pine genkey:cert:csr');
    await run('pine genkey:cert:cert');
  },
  'ensure:generated': async () => {
    await shell(`mkdir -p "${GENERATED_DIR}"`);
  },
  pw: async () => {
    if (!fs.existsSync(PW_CA_FILE)) {
      fs.writeFileSync(PW_CA_FILE, `${uuid.v4().replace(/-/g, '')}\n`);
    }
    if (!fs.existsSync(PW_CERT_FILE)) {
      fs.writeFileSync(PW_CERT_FILE, `${uuid.v4().replace(/-/g, '')}\n`);
    }
  },
  'ca:key': async () => {
    log.info(color.green('Creating CA'));
    await cmd(`openssl genrsa -des3 -passout "file:${PW_CA_FILE}" -out "${CA_KEY_FILE}" 2048`);
  },
  'ca:cert': async () => {
    log.info(color.green('Creating ROOT Cert'));
    await cmd(
      `openssl req -x509 -batch -new -nodes -passin "file:${PW_CA_FILE}" -key "${CA_KEY_FILE}" -config "${CA_CONFIG_FILE}" -sha256 -days 1825 -out "${CA_CERT_FILE}"`,
    );
  },
  'ca:install': async () => {
    log.info(color.green('Adding CA to keychain. Will prompt for sudo.'));
    await cmd(
      `sudo security add-trusted-cert -d -r trustRoot -k "/Library/Keychains/System.keychain" "${CA_CERT_FILE}"`,
    );
  },

  'cert:key': async () => {
    log.info(color.green('Creating private key.'));
    await cmd(`openssl genrsa -passout "file:${PW_CERT_FILE}" -out "${PRIVATE_KEY_FILE}" 2048`);
  },
  'cert:csr': async () => {
    log.info(color.green('Creating request.'));
    await cmd(
      `openssl req -new -batch -key "${PRIVATE_KEY_FILE}" -passin "file:${PW_CERT_FILE}" -config "${CSR_CONFIG_FILE}" -out "${CSR_FILE}"`,
    );
  },
  'cert:cert': async () => {
    log.info(color.green('Creating certificate.'));
    await cmd(
      `openssl x509 -req -in "${CSR_FILE}" -CA "${CA_CERT_FILE}" -CAkey "${CA_KEY_FILE}" -passin "file:${PW_CA_FILE}" -CAcreateserial -out "${CERT_FILE}" -days 365 -sha256 -extfile "${EXT_CONFIG_FILE}"`,
    );
  },
};
