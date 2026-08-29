const state = { accessToken: null, refreshToken: null };
const password = document.querySelector("#password");
const account = document.querySelector("#account");
const status = document.querySelector("#result-status");
const body = document.querySelector("#result-body");

function show(response, payload) {
    status.textContent = `${response.status} ${response.statusText}`;
    body.textContent = JSON.stringify(payload, null, 2);
}

async function request(path, options = {}) {
    const response = await fetch(path, options);
    const contentType = response.headers.get("content-type") ?? "";
    const payload = contentType.includes("application/json") ? await response.json() : { message: await response.text() };
    show(response, payload);
    return { response, payload };
}

document.querySelector("#issue-token").addEventListener("click", async () => {
    const { response, payload } = await request("/api/auth/token", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: account.value, password: password.value })
    });
    if (response.ok) {
        state.accessToken = payload.accessToken;
        state.refreshToken = payload.refreshToken;
        password.value = "";
    }
});

function bearerRequest(path) {
    if (!state.accessToken) {
        status.textContent = "Issue a token first.";
        return Promise.resolve();
    }
    return request(path, { headers: { Authorization: `Bearer ${state.accessToken}` } });
}

document.querySelector("#inspect-token").addEventListener("click", () => bearerRequest("/api/security/me"));
document.querySelector("#report").addEventListener("click", () => bearerRequest("/api/security/reports"));
document.querySelector("#admin-api").addEventListener("click", () => bearerRequest("/api/admin"));
document.querySelector("#refresh").addEventListener("click", async () => {
    if (!state.refreshToken) {
        status.textContent = "Issue a token first.";
        return;
    }
    const { response, payload } = await request("/api/auth/refresh", {
        method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ refreshToken: state.refreshToken })
    });
    if (response.ok) {
        state.accessToken = payload.accessToken;
        state.refreshToken = payload.refreshToken;
    }
});