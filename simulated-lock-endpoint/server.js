const http = require("http");
const fs = require("fs");
const path = require("path");

const PORT = Number(process.env.PORT || 4173);
const HOST = process.env.HOST || "127.0.0.1";
const PUBLIC_DIR = path.join(__dirname, "public");

const state = {
  targetId: "demo-lock-001",
  lockState: "LOCKED",
  safetyState: "Idle",
  lastUpdated: new Date().toISOString(),
  events: [],
};

const transitions = {
  target_detected: {
    from: ["Idle"],
    to: "TargetDetected",
    lockState: "LOCKED",
    label: "Target detected by XR app",
  },
  intent_held: {
    from: ["TargetDetected"],
    to: "Armed",
    lockState: "LOCKED",
    label: "Intent held, flow armed",
  },
  proximity_verified: {
    from: ["Armed"],
    to: "ProximityVerified",
    lockState: "LOCKED",
    label: "Phone proximity verified",
  },
  confirmation_required: {
    from: ["ProximityVerified"],
    to: "PhysicalConfirmationRequired",
    lockState: "LOCKED",
    label: "Physical confirmation requested",
  },
  physical_confirmed: {
    from: ["PhysicalConfirmationRequired"],
    to: "Unlocked",
    lockState: "SIMULATED_UNLOCKED",
    label: "Physical confirmation accepted",
  },
  expired: {
    from: ["TargetDetected", "Armed", "ProximityVerified", "PhysicalConfirmationRequired"],
    to: "Expired",
    lockState: "LOCKED",
    label: "Flow expired",
  },
  error: {
    from: ["Idle", "TargetDetected", "Armed", "ProximityVerified", "PhysicalConfirmationRequired"],
    to: "Error",
    lockState: "LOCKED",
    label: "Flow moved to error",
  },
  reset: {
    from: ["Idle", "TargetDetected", "Armed", "ProximityVerified", "PhysicalConfirmationRequired", "Unlocked", "Expired", "Error"],
    to: "Idle",
    lockState: "LOCKED",
    label: "Demo reset",
  },
};

function applyEvent(type, source = "dashboard", detail = "") {
  const transition = transitions[type];

  if (!transition) {
    addLog("rejected", source, `Unknown event: ${type}`);
    return { ok: false, status: 400, message: `Unknown event: ${type}` };
  }

  if (!transition.from.includes(state.safetyState)) {
    addLog(
      "rejected",
      source,
      `${type} rejected from ${state.safetyState}. No unlock state changed.`,
    );
    return {
      ok: false,
      status: 409,
      message: `${type} is not allowed from ${state.safetyState}`,
    };
  }

  state.safetyState = transition.to;
  state.lockState = transition.lockState;
  state.lastUpdated = new Date().toISOString();
  addLog(type, source, detail || transition.label);
  return { ok: true, status: 200, message: transition.label };
}

function addLog(type, source, detail) {
  state.events.unshift({
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    type,
    source,
    detail,
    safetyState: state.safetyState,
    lockState: state.lockState,
    timestamp: new Date().toISOString(),
  });

  state.events = state.events.slice(0, 80);
}

function sendJson(res, statusCode, body) {
  res.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store",
  });
  res.end(JSON.stringify(body, null, 2));
}

function readJson(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", (chunk) => {
      body += chunk;
      if (body.length > 1_000_000) {
        req.destroy();
        reject(new Error("Request body too large"));
      }
    });
    req.on("end", () => {
      if (!body) {
        resolve({});
        return;
      }
      try {
        resolve(JSON.parse(body));
      } catch (error) {
        reject(error);
      }
    });
  });
}

function contentType(filePath) {
  if (filePath.endsWith(".html")) return "text/html; charset=utf-8";
  if (filePath.endsWith(".css")) return "text/css; charset=utf-8";
  if (filePath.endsWith(".js")) return "text/javascript; charset=utf-8";
  if (filePath.endsWith(".svg")) return "image/svg+xml";
  return "application/octet-stream";
}

function serveStatic(req, res) {
  const requestPath = req.url === "/" ? "/index.html" : req.url;
  const filePath = path.normalize(path.join(PUBLIC_DIR, requestPath));

  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403);
    res.end("Forbidden");
    return;
  }

  fs.readFile(filePath, (error, data) => {
    if (error) {
      res.writeHead(404);
      res.end("Not found");
      return;
    }

    res.writeHead(200, {
      "Content-Type": contentType(filePath),
      "Cache-Control": "no-store",
    });
    res.end(data);
  });
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`);

  if (req.method === "GET" && url.pathname === "/api/state") {
    sendJson(res, 200, state);
    return;
  }

  if (req.method === "POST" && url.pathname === "/api/events") {
    try {
      const body = await readJson(req);
      const result = applyEvent(body.type, body.source, body.detail);
      sendJson(res, result.status, { ...result, state });
    } catch (error) {
      sendJson(res, 400, { ok: false, message: error.message, state });
    }
    return;
  }

  if (req.method === "POST" && url.pathname === "/api/reset") {
    const result = applyEvent("reset", "dashboard");
    sendJson(res, result.status, { ...result, state });
    return;
  }

  if (req.method === "GET") {
    serveStatic(req, res);
    return;
  }

  sendJson(res, 405, { ok: false, message: "Method not allowed" });
});

server.listen(PORT, HOST, () => {
  console.log(`LookLatch simulated lock endpoint: http://${HOST}:${PORT}`);
});
