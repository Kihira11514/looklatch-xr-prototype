const lockState = document.querySelector("#lockState");
const safetyState = document.querySelector("#safetyState");
const targetId = document.querySelector("#targetId");
const eventLog = document.querySelector("#eventLog");
const lastUpdated = document.querySelector("#lastUpdated");
const statusGrid = document.querySelector(".status-grid");

async function requestJson(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
    },
    ...options,
  });
  const body = await response.json();
  if (!response.ok) {
    render(body.state);
    return body;
  }
  render(body.state || body);
  return body;
}

function render(state) {
  if (!state) return;

  lockState.textContent = state.lockState;
  safetyState.textContent = state.safetyState;
  targetId.textContent = state.targetId;
  lastUpdated.textContent = new Date(state.lastUpdated).toLocaleString();

  statusGrid.classList.toggle("state-unlocked", state.safetyState === "Unlocked");
  statusGrid.classList.toggle("state-error", state.safetyState === "Error");
  statusGrid.classList.toggle("state-expired", state.safetyState === "Expired");

  eventLog.innerHTML = "";

  if (!state.events.length) {
    const item = document.createElement("li");
    item.className = "empty";
    item.textContent = "No events yet";
    eventLog.append(item);
    return;
  }

  state.events.forEach((event) => {
    const item = document.createElement("li");
    item.className = "event";

    const type = document.createElement("span");
    type.className = "event-type";
    type.textContent = event.type;

    const detail = document.createElement("span");
    detail.className = "event-detail";
    detail.textContent = event.detail;

    const meta = document.createElement("span");
    meta.className = "event-meta";
    meta.textContent = `${event.safetyState} / ${new Date(event.timestamp).toLocaleTimeString()}`;

    item.append(type, detail, meta);
    eventLog.append(item);
  });
}

document.querySelectorAll("[data-event]").forEach((button) => {
  button.addEventListener("click", async () => {
    await requestJson("/api/events", {
      method: "POST",
      body: JSON.stringify({
        type: button.dataset.event,
        source: "dashboard",
      }),
    });
  });
});

document.querySelector("#resetButton").addEventListener("click", async () => {
  await requestJson("/api/reset", { method: "POST" });
});

requestJson("/api/state");
setInterval(() => requestJson("/api/state"), 3000);
