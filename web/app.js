const AGENTS = ["Unassigned", "Aditya", "Riya", "Karan", "Meera"];
const STATUS = { OPEN: "Open", IN_PROGRESS: "In Progress", RESOLVED: "Resolved", CLOSED: "Closed" };
let tickets = [];
let filter = "ALL";

const $ = id => document.getElementById(id);
const esc = s => String(s).replace(/[&<>"]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));

// Get data from the Java server
async function load() {
  const data = await (await fetch("/api/tickets")).json();
  tickets = data.tickets;
  renderStats(data.stats);
  render();
}

// Send data to the Java server
async function post(url, data) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams(data)
  });
  if (!res.ok) alert((await res.json()).error);
  load();
}

function renderStats(s) {
  const cards = [["Total Tickets", s.total], ["Open", s.open], ["In Progress", s.inProgress], ["Resolved", s.resolved], ["Critical Active", s.critical]];
  $("stats").innerHTML = cards.map(c => `<div class="stat"><span>${c[0]}</span><b>${c[1]}</b></div>`).join("");
}

function render() {
  $("chips").innerHTML = ["ALL", ...Object.keys(STATUS)].map(k =>
    `<div class="chip ${filter === k ? "on" : ""}" onclick="filter='${k}';render()">${k === "ALL" ? "All" : STATUS[k]}</div>`).join("");

  const q = $("search").value.toLowerCase();
  const shown = tickets
    .filter(t => filter === "ALL" || t.status === filter)
    .filter(t => (t.title + t.description + t.requester + t.assignee).toLowerCase().includes(q));

  $("list").innerHTML = shown.length ? shown.map(card).join("") : `<div class="empty">No tickets found 🎉</div>`;
}

function card(t) {
  const statusOpts = Object.keys(STATUS).map(k => `<option value="${k}" ${t.status === k ? "selected" : ""}>${STATUS[k]}</option>`).join("");
  const agentOpts = AGENTS.map(a => `<option ${t.assignee === a ? "selected" : ""}>${a}</option>`).join("");
  return `<div class="ticket">
    <div class="top"><span>#${t.id} · ${t.type}</span><span class="badge ${t.priority}">${t.priority}</span></div>
    <h3>${esc(t.title)}</h3>
    <p>${esc(t.description) || "No description"}</p>
    <div class="meta"><span>👤 ${esc(t.requester)}</span><span>⏱ SLA ${t.slaHours}h · ${t.createdAt}</span></div>
    <div class="controls">
      <select onchange="post('/api/tickets/update',{id:${t.id},status:this.value})">${statusOpts}</select>
      <select onchange="post('/api/tickets/update',{id:${t.id},assignee:this.value})">${agentOpts}</select>
      <button class="del" onclick="if(confirm('Delete this ticket?'))post('/api/tickets/delete',{id:${t.id}})">🗑</button>
    </div>
  </div>`;
}

function openModal() { $("modal").classList.add("show"); }
function closeModal() { $("modal").classList.remove("show"); }

async function createTicket() {
  await post("/api/tickets", {
    title: $("f-title").value, description: $("f-desc").value, requester: $("f-req").value || "Unknown",
    type: $("f-type").value, priority: $("f-pri").value
  });
  ["f-title", "f-desc", "f-req"].forEach(id => $(id).value = "");
  closeModal();
}

load();
