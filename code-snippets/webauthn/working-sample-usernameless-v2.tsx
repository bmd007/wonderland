const onISUVPAA = async () => {
    if (window.PublicKeyCredential) {
        if (PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable) {
            const result = await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();
            if (result) {
                console.log('User Verifying Platform Authenticator is *available*.');
            } else {
                console.log('User Verifying Platform Authenticator is not available.');
            }
        } else {
            console.log('IUVPAA function is not available.');
        }
    } else {
        console.log('PublicKeyCredential is not available.');
    }
};

async function webauthnAuthenticationWithNoUsernameAndNoUserHandle() {
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
    };
    try {
        const { data: loginResponse } = await api.postJson(
            Authentication.webAuthn(country),
            body,
        );
    } catch (e) {
        console.error(`webauthn authentication failed`, e);
    }
}

async function registerWebAuthnOnThisDevice() {
    const registrationRequest = (
        await api.post('/authentication/v1/methods/webauthn/register', {
            displayName: fullName,
            credentialNickname: `this-device-credential-${random(999999)}`,
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
        alert(`Enabled password less login for ${finishRegistrationResponse.username}`); 
    } else {
        alert(`Failed to enable password less login for ${finishRegistrationResponse.username}`); 
    }
}

async function registerWebAuthnOnAnotherDevice() {
    const registrationRequest = (
        await api.post('/authentication/v1/methods/webauthn/register/cross-platform', {
            displayName: fullName,
            credentialNickname: `another-device-credential-${random(999999)}`,
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
        alert(`Enabled password less login for ${finishRegistrationResponse.username}`); 
    } else {
        alert(`Failed to enable password less login for ${finishRegistrationResponse.username}`); 
    }
}
