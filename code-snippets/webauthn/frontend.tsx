
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
        console.log('PublicKeyCredential is not availlable.');
    }
};

async function webauthn_register_local() {
    const registrationRequest = await fetch(
        'https://localhost.localdomain/v1/methods/webauthn/register/local',
        {
            body: JSON.stringify({
                displayName: `Robin`,
                credentialNickname: `webapp-next-mac-credential`,
            }),
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
        },
    ).then(response => response.json());
    const options = {
        publicKey: registrationRequest.publicKeyCredentialCreationOptions,
    };
    const credential = await create(options);
    const payload = { requestId: registrationRequest.requestId, credential };
    const finishRegistrationResponse = await fetch(
        'https://localhost.localdomain/v1/methods/webauthn/register/finish/local',
        {
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
            body: JSON.stringify(payload),
        },
    ).then(response => response.json());
    if (finishRegistrationResponse.success) {
        localStorage.setItem('WebAuthN-Username', finishRegistrationResponse.username);
    }
    console.log(`finishRegistrationResponse.username : ${finishRegistrationResponse.username}`);
}

async function webauthn_register_cross_platform_local() {
    const registrationRequest = await fetch(
        'https://localhost.localdomain/v1/methods/webauthn/register/local/cross-platform',
        {
            body: JSON.stringify({
                displayName: `Robin`,
                credentialNickname: `webapp-next-mac-credential`,
            }),
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
        },
    ).then(response => response.json());
    const options = {
        publicKey: registrationRequest.publicKeyCredentialCreationOptions,
    };
    const credential = await create(options);
    const payload = { requestId: registrationRequest.requestId, credential };
    const finishRegistrationResponse = await fetch(
        'https://localhost.localdomain/v1/methods/webauthn/register/finish/local',
        {
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
            body: JSON.stringify(payload),
        },
    ).then(response => response.json());
    if (finishRegistrationResponse.success) {
        localStorage.setItem(
            'WebAuthN-userHandle',
            registrationRequest.publicKeyCredentialCreationOptions.user.id,
        );
    }
    console.log(`finishRegistrationResponse.username : ${finishRegistrationResponse.username}`);
}

async function webauthn_authenticate_local() {
    // todo think if it's needed to use userHandle here to avoid saving username on the machine.
    // on MAC, so far, no browser can do this process without a username or userhandle
    // on chrome, which at least does something, at the end, the response lacks userHandle. Well sometimes it does. Apparently, there is some flaky ness in my setup
    // however, yubikey and android return userHandle in their auth response.
    const usernameSavedInLocalStorage = localStorage.getItem('WebAuthN-Username');
    if (!usernameSavedInLocalStorage || usernameSavedInLocalStorage.trim().length === 0) {
        alert(`no username, register first`);
        console.log(localStorage);
        throw new Error('no username, register first!');
    }
    const authenticationRequest = await fetch(
        'https://localhost.localdomain/v1/methods/webauthn/authenticate',
        {
            body: JSON.stringify({
                username: usernameSavedInLocalStorage,
                countryCode: country,
            }),
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
        },
    ).then(response => response.json());
    const options = {
        publicKey: authenticationRequest.publicKeyCredentialRequestOptions,
    };
    const credential = await get(options);
    const payload = { requestId: authenticationRequest.requestId, credential };
    const finishAuthenticationResponse = await fetch(
        `https://localhost.localdomain/v1/methods/webauthn/authenticate/finish?country=${country}`,
        {
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
            body: JSON.stringify(payload),
        },
    ).then(response => response.json());
    console.log(`finishAuthenticationResponse.username : ${finishAuthenticationResponse.username}`);
}

async function webauthn_authenticate_local_cross_platform_without_user_handle() {
    const authenticationRequest = await fetch(
        'https://localhost.localdomain/v1/methods/webauthn/authenticate',
        {
            body: JSON.stringify({
                countryCode: country,
            }),
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
        },
    ).then(response => response.json());
    const options = {
        publicKey: authenticationRequest.publicKeyCredentialRequestOptions,
    };
    console.log(`public key from assertion request ${JSON.stringify(options)}`);
    const credential = await get(options);
    const payload = { requestId: authenticationRequest.requestId, credential };
    const finishAuthenticationResponse = await fetch(
        `https://localhost.localdomain/v1/methods/webauthn/authenticate/finish?country=${country}`,
        {
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
            body: JSON.stringify(payload),
        },
    ).then(response => response.json());
    console.log(`finishAuthenticationResponse.username : ${finishAuthenticationResponse.username}`);
}

async function webauthn_authenticate_local_cross_platform() {
    const userHandleSavedInLocalStorage = localStorage.getItem('WebAuthN-userHandle');
    if (!userHandleSavedInLocalStorage || userHandleSavedInLocalStorage.trim().length === 0) {
        alert(`no userHandle, register first`);
        console.log(localStorage);
        throw new Error('no userHandle, register first!');
    }
    const authenticationRequest = await fetch(
        'https://localhost.localdomain/v1/methods/webauthn/authenticate',
        {
            body: JSON.stringify({
                userHandle: userHandleSavedInLocalStorage,
                countryCode: country,
            }),
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
        },
    ).then(response => response.json());
    const options = {
        publicKey: authenticationRequest.publicKeyCredentialRequestOptions,
    };
    const credential = await get(options);
    const payload = { requestId: authenticationRequest.requestId, credential };
    const finishAuthenticationResponse = await fetch(
        `https://localhost.localdomain/v1/methods/webauthn/authenticate/finish?country=${country}`,
        {
            method: 'POST',
            headers: {
                'Content-type': 'application/json',
            },
            body: JSON.stringify(payload),
        },
    ).then(response => response.json());
    console.log(`finishAuthenticationResponse.username : ${finishAuthenticationResponse.username}`);
}
