const ROLE_KEY = "ums_role";
const EMAIL_KEY = "ums_email";

// Which resources each role is allowed to manage (mirrors SecurityConfig).
const ROLE_ACCESS = {
    ROLE_ADMIN: ["admins", "teachers", "students"],
    ROLE_TEACHER: ["teachers"],
    ROLE_STUDENT: ["students"]
};

const RESOURCE_LABELS = {
    admins: "Admins",
    teachers: "Teachers",
    students: "Students"
};

function saveLogin(email, role) {
    sessionStorage.setItem(EMAIL_KEY, email);
    sessionStorage.setItem(ROLE_KEY, role);
}

function clearLogin() {
    sessionStorage.removeItem(EMAIL_KEY);
    sessionStorage.removeItem(ROLE_KEY);
}

function getRole() {
    return sessionStorage.getItem(ROLE_KEY);
}

function getEmail() {
    return sessionStorage.getItem(EMAIL_KEY);
}

// "ROLE_ADMIN" -> "ADMIN"
function displayRole(role) {
    return role ? role.replace("ROLE_", "") : "";
}

function canAccess(resource) {
    const allowed = ROLE_ACCESS[getRole()] || [];
    return allowed.includes(resource);
}

// Sends the user back to the login page if nobody is logged in.
function requireLogin() {
    if (!getRole()) {
        window.location.href = "index.html";
        return false;
    }
    return true;
}

// Calls the REST API and returns the parsed JSON body.
// Throws an Error with a readable message for any non-2xx status.
async function apiRequest(method, url, body) {
    const options = {
        method: method,
        headers: { "Accept": "application/json" }
    };

    if (body !== undefined) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);

    // 401 = session missing or expired -> back to the login page
    if (response.status === 401) {
        clearLogin();
        window.location.href = "index.html?expired=1";
        throw new Error("Your session has expired. Please log in again.");
    }

    // 204 No Content (e.g. after DELETE) has no body
    const text = await response.text();
    let data = null;
    if (text) {
        try {
            data = JSON.parse(text);
        } catch (e) {
            data = text;
        }
    }

    if (!response.ok) {
        throw new Error(describeError(response.status, data));
    }

    return data;
}

// Turns the backend's error JSON into one readable line.
function describeError(status, data) {
    if (data && typeof data === "object") {
        let text = data.message || data.error || "Request failed";

        // Validation errors from GlobalExceptionHandler
        if (data.fieldErrors) {
            const details = Object.entries(data.fieldErrors)
                .map(([field, msg]) => field + ": " + msg);
            text += " (" + details.join("; ") + ")";
        }

        return "Error " + status + ": " + text;
    }

    return "Error " + status + ": request failed";
}

// Shows a status line. With no CSS, errors are marked by their text.
function showMessage(element, text, isError) {
    element.textContent = "";
    if (!text) {
        return;
    }
    const strong = document.createElement("strong");
    strong.textContent = isError ? text : "Success: " + text;
    element.appendChild(strong);
}

async function logout() {
    try {
        await fetch("/logout", { method: "POST" });
    } finally {
        clearLogin();
        window.location.href = "index.html";
    }
}

// Builds the top navigation line: user info + allowed links + logout.
function renderNav(containerId) {
    const nav = document.getElementById(containerId);
    nav.textContent = "";

    const who = document.createElement("span");
    who.textContent = "Logged in as " + getEmail() + " (" + displayRole(getRole()) + ")";
    nav.appendChild(who);
    nav.appendChild(document.createElement("br"));
    nav.appendChild(document.createElement("br"));

    const links = [{ href: "dashboard.html", text: "Dashboard" }];
    for (const resource of ROLE_ACCESS[getRole()] || []) {
        links.push({ href: resource + ".html", text: RESOURCE_LABELS[resource] });
    }

    links.forEach((link, index) => {
        if (index > 0) {
            nav.appendChild(document.createTextNode(" | "));
        }
        const a = document.createElement("a");
        a.href = link.href;
        a.textContent = link.text;
        nav.appendChild(a);
    });

    nav.appendChild(document.createTextNode(" | "));
    const logoutButton = document.createElement("button");
    logoutButton.type = "button";
    logoutButton.textContent = "Logout";
    logoutButton.addEventListener("click", logout);
    nav.appendChild(logoutButton);
}
