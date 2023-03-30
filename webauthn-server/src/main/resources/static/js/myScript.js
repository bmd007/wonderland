let ceremonyState = {}

function extend (obj, more) {
  return Object.assign({}, obj, more)
}

function rejectIfNotSuccess (response) {
  console.log(response)
  if (response.success) {
    return response
  } else {
    return new Promise((resolve, reject) => reject(response))
  }
}

function rejected (err) {
  return new Promise((resolve, reject) => reject(err))
}

function setStatus (statusText) {
  document.getElementById('status').textContent = statusText
}

function addMessage (message) {
  const el = document.getElementById('messages')
  const p = document.createElement('p')
  p.appendChild(document.createTextNode(message))
  el.appendChild(p)
}

function addMessages (messages) {
  messages.forEach(addMessage)
}

function clearMessages () {
  const el = document.getElementById('messages')
  while (el.firstChild) {
    el.removeChild(el.firstChild)
  }
}

function showJson (name, data) {
  const el = document.getElementById(name)
    .textContent = JSON.stringify(data, false, 2)
}

function showRequest (data) {
  return showJson('request', data)
}

function showAuthenticatorResponse (data) {
  const clientDataJson = data && (data.response && data.response.clientDataJSON)
  return showJson('authenticator-response', extend(
    data, {
      _clientDataJson: data && JSON.parse(new TextDecoder('utf-8').decode(base64url.toByteArray(clientDataJson))),
    }))
}

function showServerResponse (data) {
  if (data && data.messages) {
    addMessages(data.messages)
  }
  return showJson('server-response', data)
}

function hideDeviceInfo () {
  document.getElementById('device-info').style = 'display: none'
}

function showDeviceInfo (params) {
  document.getElementById('device-info').style = undefined

  if (params.displayName) {
    document.getElementById('device-name-row').style = undefined
    document.getElementById('device-name').textContent = params.displayName
  } else {
    document.getElementById('device-name-row').style = 'display: none'
  }

  if (params.nickname) {
    document.getElementById('device-nickname-row').style = undefined
    document.getElementById('device-nickname').textContent = params.nickname
  } else {
    document.getElementById('device-nickname-row').style = 'display: none'
  }

  if (params.imageUrl) {
    document.getElementById('device-icon').src = params.imageUrl
  }
}

function resetDisplays () {
  clearMessages()
  showRequest(null)
  showAuthenticatorResponse(null)
  showServerResponse(null)
  hideDeviceInfo()
}

function getIndexActions () {
  return fetch('https://localhost.localdomain/actions', {
    headers: {
      'Content-Type': 'application/json',
      // 'Origin': 'https://localhost.localdomain',
    }
  })
    .then(response => response.json())
    .then(data => data.actions)
}

function getRegisterRequest (urls, username, displayName, credentialNickname, requireResidentKey) {
  return fetch('https://localhost.localdomain/register', {
    body: new URLSearchParams({
      username,
      displayName: displayName || username,
      credentialNickname,
      requireResidentKey: requireResidentKey || 'preferred'
    }),
    method: 'POST',
    // headers: {
    //   'Origin': 'https://localhost.localdomain',
    // }
  })
    .then(response => response.json())
    .then(rejectIfNotSuccess)
}

function executeRegisterRequest (request) {
  console.log('executeRegisterRequest', request)
  return create({ publicKey: request.publicKeyCredentialCreationOptions })
}

function submitResponse (url, request, response) {
  console.log('submitResponse', url, request, response)
  const body = { requestId: request.requestId, credential: response }
  console.log("finish body: "+ JSON.stringify(body))
  return fetch('https://localhost.localdomain/register/finish', {
    method: 'POST',
    body: JSON.stringify(body),
    headers: {
      'Content-Type': 'application/json',
    }
  })
    .then(response => response.json())
}

async function performCeremony (params) {
  const callbacks = params.callbacks || {} /* { init, authenticatorRequest, serverRequest } */
  const getIndexActions = params.getIndexActions /* function(): object */
  const getRequest = params.getRequest /* function(urls: object): { publicKeyCredentialCreationOptions: object } | { publicKeyCredentialRequestOptions: object } */
  const statusStrings = params.statusStrings /* { init, authenticatorRequest, serverRequest, success, } */
  const executeRequest = params.executeRequest /* function({ publicKeyCredentialCreationOptions: object } | { publicKeyCredentialRequestOptions: object }): Promise[PublicKeyCredential] */
  const handleError = params.handleError /* function(err): ? */

  setStatus('Looking up API paths...')
  resetDisplays()

  const rootUrls = await getIndexActions()

  setStatus(statusStrings.int)
  if (callbacks.init) {
    callbacks.init(rootUrls)
  }
  const { request, actions: urls } = await getRequest(rootUrls)

  setStatus(statusStrings.authenticatorRequest)
  if (callbacks.authenticatorRequest) {
    callbacks.authenticatorRequest({ request, urls })
  }
  showRequest(request)
  ceremonyState = {
    callbacks,
    request,
    statusStrings,
    urls,
  }

  const webauthnResponse = await executeRequest(request)
  return await finishCeremony(webauthnResponse)
}

async function finishCeremony (response) {
  const callbacks = ceremonyState.callbacks
  const request = ceremonyState.request
  const statusStrings = ceremonyState.statusStrings
  const urls = ceremonyState.urls

  setStatus(statusStrings.serverRequest || 'Sending response to server...')
  if (callbacks.serverRequest) {
    callbacks.serverRequest({ urls, request, response })
  }
  showAuthenticatorResponse(response)

  const data = await submitResponse(urls.finish, request, response)

  if (data && data.success) {
    setStatus(statusStrings.success)
  } else {
    setStatus('Error!')
  }
  showServerResponse(data)

  return data
}

async function registerPrefilledButton (event) {
  const authnRequestJson = {
    publicKey:
      {
        challenge: Uint8Array.from('a challenge', c => c.charCodeAt(0)),
        rp: {
          name: 'LA@Nordnet',
          // id: 'la@nordnet.se',
        },
        user: {
          id: Uint8Array.from('e9a7b89d-1a2b-4f44-87ac-698c89bc7e11', c => c.charCodeAt(0)),
          name: 'robsan@nordnet.se',
          displayName: 'Robin',
        },
        pubKeyCredParams: [{ alg: -7, type: 'public-key' }],
        authenticatorSelection: {
          authenticatorAttachment: 'cross-platform',
        },
        timeout: 60000,
        attestation: 'direct',
      }
  }

  const credential = await navigator.credentials.get(authnRequestJson)
  const webAuthNRespose = getResponseToJSON(credential)
  console.log(webAuthNRespose)
}

function registerResidentKey (event) {
  return register(event, 'required')
}

async function register (event, requireResidentKey) {
  const username = document.getElementById('username').value
  const displayName = document.getElementById('displayName').value
  const credentialNickname = document.getElementById('credentialNickname').value

  var request

  try {
    const data = await performCeremony({
      getIndexActions,
      getRequest: urls => getRegisterRequest(urls, username, displayName, credentialNickname, requireResidentKey),
      statusStrings: {
        init: 'Initiating registration ceremony with server...',
        authenticatorRequest: 'Asking authenticators to create credential...',
        success: 'Registration successful!',
      },
      executeRequest: req => {
        request = req
        return executeRegisterRequest(req)
      },
    })

    console.log('data after registration in backend: ' + data)
    if (data.registration) {
      const nicknameInfo = {
        nickname: data.registration.credentialNickname,
      }

      if (data.registration && data.registration.attestationMetadata) {
        showDeviceInfo(extend(
          data.registration.attestationMetadata.deviceProperties,
          nicknameInfo
        ))
      } else {
        showDeviceInfo(nicknameInfo)
      }

      if (!data.attestationTrusted) {
        addMessage('Warning: Attestation is not trusted!')
      }
    }

  } catch (err) {
    console.error('Registration failed', err)
    setStatus('Registration failed.')

    if (err.name === 'NotAllowedError') {
      if (request.publicKeyCredentialCreationOptions.excludeCredentials
        && request.publicKeyCredentialCreationOptions.excludeCredentials.length > 0
      ) {
        addMessage('Credential creation failed, probably because an already registered credential is avaiable.')
      } else {
        addMessage('Credential creation failed for an unknown reason.')
      }
    } else if (err.name === 'InvalidStateError') {
      addMessage(`This authenticator is already registered for the account "${username}". Please try again with a different authenticator.`)
    } else if (err.message) {
      addMessage(`${err.name}: ${err.message}`)
    } else if (err.messages) {
      addMessages(err.messages)
    }
    return rejected(err)
  }
}

function getAuthenticateRequest (urls, username) {
  return fetch(urls.authenticate, {
    body: new URLSearchParams(username ? { username } : {}),
    method: 'POST',
  })
    .then(response => response.json())
    .then(rejectIfNotSuccess)
}

function executeAuthenticateRequest (request) {
  console.log('executeAuthenticateRequest', request)
  return get({ publicKey: request.publicKeyCredentialRequestOptions })
}

function authenticateWithUsername (event) {
  return authenticate(event, document.getElementById('username').value)
}

async function authenticate (event, username) {
  try {
    const data = await performCeremony({
      getIndexActions,
      getRequest: urls => getAuthenticateRequest(urls, username),
      statusStrings: {
        init: 'Initiating authentication ceremony with server...',
        authenticatorRequest: 'Asking authenticators to perform assertion...',
        success: 'Authentication successful!',
      },
      executeRequest: executeAuthenticateRequest,
    })

    if (data.registrations) {
      addMessage(`Authenticated as: ${data.registrations[0].username}`)
    }
    return data

  } catch (err) {
    setStatus('Authentication failed.')
    if (err.name === 'InvalidStateError') {
      addMessage(`This authenticator is not registered for the account "${username}". Please try again with a registered authenticator.`)
    } else if (err.message) {
      addMessage(`${err.name}: ${err.message}`)
    } else if (err.messages) {
      addMessages(err.messages)
    }
    console.error('Authentication failed', err)
    return rejected(err)
  }
}

function deregister () {
  const credentialId = document.getElementById('deregisterCredentialId').value
  addMessage('Deregistering credential...')

  return getIndexActions()
    .then(urls =>
      fetch(urls.deregister, {
        body: new URLSearchParams({ credentialId }),
        method: 'POST',
      })
    )
    .then(response => response.json())
    .then(rejectIfNotSuccess)
    .then(data => {
      if (data.success) {
        if (data.droppedRegistration) {
          addMessage(`Successfully deregistered credential: ${data.droppedRegistration.credentialNickname || credentialId}`)
        } else {
          addMessage(`Successfully deregistered credential: ${credentialId}`)
        }
        if (data.accountDeleted) {
          addMessage('No credentials remain - account deleted.')
        }
      } else {
        addMessage('Credential deregistration failed.')
      }
    })
    .catch((err) => {
      setStatus('Credential deregistration failed.')
      if (err.message) {
        addMessage(`${err.name}: ${err.message}`)
      } else if (err.messages) {
        addMessages(err.messages)
      }
      console.error('Authentication failed', err)
      return rejected(err)
    })
}

function usernameChanged (event) {
  const displayNameField = document.getElementById('displayName')
  displayNameField.placeholder = event.target.value
}

function init () {
  hideDeviceInfo()

  document.getElementById('username').oninput = usernameChanged
  document.getElementById('registerButton').onclick = register
  document.getElementById('registerRkButton').onclick = registerResidentKey
  document.getElementById('registerPrefilledButton').onclick = registerPrefilledButton
  document.getElementById('authenticateWithUsernameButton').onclick = authenticateWithUsername
  document.getElementById('authenticateButton').onclick = authenticate
  document.getElementById('deregisterButton').onclick = deregister

  return false
}

window.onload = init

// src/webauthn-json/base64url.ts
function base64urlToBuffer (baseurl64String) {
  const padding = '=='.slice(0, (4 - baseurl64String.length % 4) % 4)
  const base64String = baseurl64String.replace(/-/g, '+').replace(/_/g, '/') + padding
  const str = atob(base64String)
  const buffer = new ArrayBuffer(str.length)
  const byteView = new Uint8Array(buffer)
  for (let i = 0; i < str.length; i++) {
    byteView[i] = str.charCodeAt(i)
  }
  return buffer
}

function bufferToBase64url (buffer) {
  const byteView = new Uint8Array(buffer)
  let str = ''
  for (const charCode of byteView) {
    str += String.fromCharCode(charCode)
  }
  const base64String = btoa(str)
  const base64urlString = base64String.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')
  return base64urlString
}

// src/webauthn-json/convert.ts
var copyValue = 'copy'
var convertValue = 'convert'

function convert (conversionFn, schema2, input) {
  if (schema2 === copyValue) {
    return input
  }
  if (schema2 === convertValue) {
    return conversionFn(input)
  }
  if (schema2 instanceof Array) {
    return input.map((v) => convert(conversionFn, schema2[0], v))
  }
  if (schema2 instanceof Object) {
    const output = {}
    for (const [key, schemaField] of Object.entries(schema2)) {
      if (schemaField.deriveFn) {
        const v = schemaField.deriveFn(input)
        if (v !== void 0) {
          input[key] = v
        }
      }
      if (!(key in input)) {
        if (schemaField.required) {
          throw new Error(`Missing key: ${key}`)
        }
        continue
      }
      if (input[key] == null) {
        output[key] = null
        continue
      }
      output[key] = convert(conversionFn, schemaField.schema, input[key])
    }
    return output
  }
}

function derived (schema2, deriveFn) {
  return {
    required: true,
    schema: schema2,
    deriveFn
  }
}

function required (schema2) {
  return {
    required: true,
    schema: schema2
  }
}

function optional (schema2) {
  return {
    required: false,
    schema: schema2
  }
}

// src/webauthn-json/basic/schema.ts
var publicKeyCredentialDescriptorSchema = {
  type: required(copyValue),
  id: required(convertValue),
  transports: optional(copyValue)
}
var simplifiedExtensionsSchema = {
  appid: optional(copyValue),
  appidExclude: optional(copyValue),
  credProps: optional(copyValue)
}
var simplifiedClientExtensionResultsSchema = {
  appid: optional(copyValue),
  appidExclude: optional(copyValue),
  credProps: optional(copyValue)
}
var credentialCreationOptions = {
  publicKey: required({
    rp: required(copyValue),
    user: required({
      id: required(convertValue),
      name: required(copyValue),
      displayName: required(copyValue)
    }),
    challenge: required(convertValue),
    pubKeyCredParams: required(copyValue),
    timeout: optional(copyValue),
    excludeCredentials: optional([publicKeyCredentialDescriptorSchema]),
    authenticatorSelection: optional(copyValue),
    attestation: optional(copyValue),
    extensions: optional(simplifiedExtensionsSchema)
  }),
  signal: optional(copyValue)
}
var publicKeyCredentialWithAttestation = {
  type: required(copyValue),
  id: required(copyValue),
  rawId: required(convertValue),
  response: required({
    clientDataJSON: required(convertValue),
    attestationObject: required(convertValue),
    transports: derived(copyValue, (response) => response.getTransports?.() || [])
  }),
  clientExtensionResults: derived(simplifiedClientExtensionResultsSchema, (pkc) => pkc.getClientExtensionResults())
}
var credentialRequestOptions = {
  mediation: optional(copyValue),
  publicKey: required({
    challenge: required(convertValue),
    timeout: optional(copyValue),
    rpId: optional(copyValue),
    allowCredentials: optional([publicKeyCredentialDescriptorSchema]),
    userVerification: optional(copyValue),
    extensions: optional(simplifiedExtensionsSchema)
  }),
  signal: optional(copyValue)
}
var publicKeyCredentialWithAssertion = {
  type: required(copyValue),
  id: required(copyValue),
  rawId: required(convertValue),
  response: required({
    clientDataJSON: required(convertValue),
    authenticatorData: required(convertValue),
    signature: required(convertValue),
    userHandle: required(convertValue)
  }),
  clientExtensionResults: derived(simplifiedClientExtensionResultsSchema, (pkc) => pkc.getClientExtensionResults())
}
var schema = {
  credentialCreationOptions,
  publicKeyCredentialWithAttestation,
  credentialRequestOptions,
  publicKeyCredentialWithAssertion
}

// src/webauthn-json/basic/api.ts
function createRequestFromJSON (requestJSON) {
  return convert(base64urlToBuffer, credentialCreationOptions, requestJSON)
}

function createResponseToJSON (credential) {
  return convert(bufferToBase64url, publicKeyCredentialWithAttestation, credential)
}

async function create (requestJSON) {
  console.log('about to call navigator.create: ' + supported())
  const credential = await navigator.credentials.create(createRequestFromJSON(requestJSON))
  return createResponseToJSON(credential)
}

function getRequestFromJSON (requestJSON) {
  return convert(base64urlToBuffer, credentialRequestOptions, requestJSON)
}

function getResponseToJSON (credential) {
  return convert(bufferToBase64url, publicKeyCredentialWithAssertion, credential)
}

async function get (requestJSON) {
  const credential = await navigator.credentials.get(getRequestFromJSON(requestJSON))
  return getResponseToJSON(credential)
}

// src/webauthn-json/basic/supported.ts
function supported () {
  return !!(navigator.credentials && navigator.credentials.create && navigator.credentials.get && window.PublicKeyCredential)
}
