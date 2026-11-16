function setupCrudPage(config) {
    if (!requireLogin()) {
        return;
    }

    renderNav("nav");

    const message = document.getElementById("message");
    const content = document.getElementById("content");

    // Hide the page for roles that cannot use it.
    // (The backend would answer 403 anyway; this just avoids a broken page.)
    if (!canAccess(config.resource)) {
        content.hidden = true;
        showMessage(message, "Your role (" + displayRole(getRole()) +
            ") cannot manage " + config.resource + ".", true);
        return;
    }

    const form = document.getElementById("record-form");
    const formTitle = document.getElementById("form-title");
    const formFields = document.getElementById("form-fields");
    const saveButton = document.getElementById("save-button");
    const cancelButton = document.getElementById("cancel-button");
    const refreshButton = document.getElementById("refresh-button");
    const tableHead = document.getElementById("table-head");
    const tableBody = document.getElementById("table-body");

    // Columns shown in the list: id + every field except the password
    const columns = [{ name: "id", label: "ID" }]
        .concat(config.fields.filter(f => f.type !== "password"));

    let editingId = null;   // null = creating, number = editing that record

    buildForm();
    buildTableHead();
    resetForm();
    loadRecords();

    form.addEventListener("submit", saveRecord);
    cancelButton.addEventListener("click", resetForm);
    refreshButton.addEventListener("click", loadRecords);

    // ---------- form ----------

    function buildForm() {
        for (const field of config.fields) {
            const row = document.createElement("tr");

            const labelCell = document.createElement("td");
            const label = document.createElement("label");
            label.htmlFor = "field-" + field.name;
            label.textContent = field.label;
            labelCell.appendChild(label);

            const inputCell = document.createElement("td");
            const input = document.createElement("input");
            input.type = field.type || "text";
            input.id = "field-" + field.name;
            input.name = field.name;
            input.size = 35;
            input.required = true;
            if (field.type === "password") {
                input.minLength = 8;
                input.autocomplete = "new-password";
            }
            inputCell.appendChild(input);

            // Small hint shown next to the password while editing
            if (field.type === "password") {
                const hint = document.createElement("small");
                hint.id = "password-hint";
                hint.textContent = "  (at least 8 characters)";
                inputCell.appendChild(hint);
            }

            row.appendChild(labelCell);
            row.appendChild(inputCell);
            formFields.appendChild(row);
        }
    }

    function resetForm() {
        editingId = null;
        form.reset();
        formTitle.textContent = "Add " + config.singular;
        saveButton.textContent = "Create " + config.singular;
        cancelButton.hidden = true;
        setPasswordMode(false);
    }

    // When editing, the password is optional (blank = keep current one)
    function setPasswordMode(editing) {
        const password = form.elements["password"];
        const hint = document.getElementById("password-hint");
        if (!password) {
            return;
        }
        password.required = !editing;
        hint.textContent = editing
            ? "  (leave blank to keep the current password)"
            : "  (at least 8 characters)";
    }

    function startEdit(record) {
        editingId = record.id;
        for (const field of config.fields) {
            form.elements[field.name].value =
                field.type === "password" ? "" : (record[field.name] ?? "");
        }
        formTitle.textContent = "Edit " + config.singular + " #" + record.id;
        saveButton.textContent = "Update " + config.singular;
        cancelButton.hidden = false;
        setPasswordMode(true);
        showMessage(message, "");
        window.scrollTo(0, 0);
        form.elements[config.fields[0].name].focus();
    }

    async function saveRecord(event) {
        event.preventDefault();

        // Build the JSON body from the inputs (names match the DTO)
        const body = {};
        for (const field of config.fields) {
            const raw = form.elements[field.name].value;
            body[field.name] = field.type === "password" ? raw : raw.trim();
        }

        // Blank password on update -> send null so the backend keeps the old one
        if (editingId !== null && body.password === "") {
            body.password = null;
        }

        saveButton.disabled = true;
        try {
            if (editingId === null) {
                const created = await apiRequest("POST", config.apiUrl, body);
                showMessage(message, config.singular + " #" + created.id + " created.");
            } else {
                await apiRequest("PUT", config.apiUrl + "/" + editingId, body);
                showMessage(message, config.singular + " #" + editingId + " updated.");
            }
            resetForm();
            await loadRecords();
        } catch (error) {
            showMessage(message, error.message, true);
        } finally {
            saveButton.disabled = false;
        }
    }

    // ---------- table ----------

    function buildTableHead() {
        const row = document.createElement("tr");
        for (const column of columns) {
            const th = document.createElement("th");
            th.textContent = column.label;
            row.appendChild(th);
        }
        const actions = document.createElement("th");
        actions.textContent = "Actions";
        row.appendChild(actions);
        tableHead.appendChild(row);
    }

    async function loadRecords() {
        try {
            const records = await apiRequest("GET", config.apiUrl);
            renderRows(records || []);
        } catch (error) {
            showMessage(message, error.message, true);
        }
    }

    function renderRows(records) {
        tableBody.textContent = "";

        if (records.length === 0) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");
            cell.colSpan = columns.length + 1;
            cell.align = "center";
            cell.textContent = "No " + config.resource + " found.";
            row.appendChild(cell);
            tableBody.appendChild(row);
            return;
        }

        for (const record of records) {
            const row = document.createElement("tr");

            for (const column of columns) {
                const cell = document.createElement("td");
                // textContent (not innerHTML) so user data can't inject HTML
                cell.textContent = record[column.name] ?? "";
                row.appendChild(cell);
            }

            const actions = document.createElement("td");
            actions.noWrap = true;

            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.textContent = "Edit";
            editButton.addEventListener("click", () => startEdit(record));

            const deleteButton = document.createElement("button");
            deleteButton.type = "button";
            deleteButton.textContent = "Delete";
            deleteButton.addEventListener("click", () => deleteRecord(record));

            actions.appendChild(editButton);
            actions.appendChild(document.createTextNode(" "));
            actions.appendChild(deleteButton);
            row.appendChild(actions);

            tableBody.appendChild(row);
        }
    }

    async function deleteRecord(record) {
        const ok = window.confirm(
            "Delete " + config.singular.toLowerCase() + " #" + record.id +
            " (" + record.name + ")? This cannot be undone.");
        if (!ok) {
            return;
        }
        try {
            await apiRequest("DELETE", config.apiUrl + "/" + record.id);
            if (editingId === record.id) {
                resetForm();
            }
            showMessage(message, config.singular + " #" + record.id + " deleted.");
            await loadRecords();
        } catch (error) {
            showMessage(message, error.message, true);
        }
    }
}
