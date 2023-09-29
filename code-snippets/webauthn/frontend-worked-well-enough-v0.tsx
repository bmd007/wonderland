async function webauthn_register_this_device() {
    const registrationRequest = (
        await api.post('/authentication/v1/methods/webauthn/register', {
            displayName: fullName,
            credentialNickname: `webapp-yubikey-mac-credential-${random(999999)}`,
        })
    ).data;
    const options = {
        publicKey: registrationRequest.publicKeyCredentialCreationOptions,
    };
    const credential = await create(options);
    const payload = { requestId: registrationRequest.requestId, credential };
    const finishRegistrationResponse = (
        await api.post('/authentication/v1/methods/webauthn/register/finish', payload)
    ).data;
    if (finishRegistrationResponse.success && finishRegistrationResponse.username) {
        alert(`enabled password less login for ${finishRegistrationResponse.username}`);
    } else {
        alert(`failed to enable password less login for ${finishRegistrationResponse.username}`);
    }
}

async function webauthn_register_another_device() {
    const registrationRequest = (
        await api.post('/authentication/v1/methods/webauthn/register/cross-platform', {
            displayName: fullName,
            credentialNickname: `webapp-yubikey-mac-credential-${random(999999)}`,
        })
    ).data;
    const options = {
        publicKey: registrationRequest.publicKeyCredentialCreationOptions,
    };
    const credential = await create(options);
    const payload = { requestId: registrationRequest.requestId, credential };
    const finishRegistrationResponse = (
        await api.post('/authentication/v1/methods/webauthn/register/finish', payload)
    ).data;
    if (finishRegistrationResponse.success && finishRegistrationResponse.username) {
        alert(`enabled password less login for ${finishRegistrationResponse.username}`);
    } else {
        alert(`failed to enable password less login for ${finishRegistrationResponse.username}`);
    }
}

const onISUVPAA = async () => {
    if (window.PublicKeyCredential) {
        if (PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable) {
            const result = await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
            if (result) {
                alert('User Verifying Platform Authenticator is *available*.');
            } else {
                alert('User Verifying Platform Authenticator is not available.');
            }
        } else {
            alert('IUVPAA function is not available.');
        }
    } else {
        alert('PublicKeyCredential is not available.');
    }
};

async function webauthn_authenticate_no_user_handle() {
    const authenticationRequest = (
        await api.post('/authentication/v1/methods/webauthn/authenticate', {
            countryCode: country,
        })
    ).data;
    const options = {
        publicKey: authenticationRequest.publicKeyCredentialRequestOptions,
    };
    const credential = await get(options);
    const body = {
        assertionResponse: { requestId: authenticationRequest.requestId, credential },
        site: country,
        system: 'NEXT',
    };
    const payload = await nnapi.postJson('/api/2/authentication/webauthn/login', body);
    const isLoggedIn = path(['data', 'logged_in'], payload);
    const sessionType = path(['data', 'session_type'], payload);
    if (isLoggedIn && sessionType === 'authenticated') {
        window.location.href = queryParams.redirectTo || '/overview';
    } else {
        console.error(`webauthn authentication failed: ${payload}`);
        alert(`webauthn authentication failed`);
    }
}
