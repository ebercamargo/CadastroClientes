const $ = (s) => document.querySelector(s);
const api = "/api/v1/clientes";
let page = 0,
  currentId = null,
  busy = false,
  requestNumber = 0;
const form = $("#cadastro"),
  modal = $("#modal");
for (const uf of "AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO".split(
  " ",
)) {
  const option = document.createElement("option");
  option.value = option.textContent = uf;
  form.elements.uf.append(option);
}
async function request(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    let error;
    try {
      error = await response.json();
    } catch {
      throw Error("Não foi possível concluir a operação.");
    }
    throw Error(
      error.erros?.join("\n") ||
        error.detail ||
        "Não foi possível concluir a operação.",
    );
  }
  return response.status === 204 ? null : response.json();
}
function cell(row, text) {
  const td = document.createElement("td");
  td.textContent = text;
  row.append(td);
  return td;
}
async function load() {
  const seq = ++requestNumber;
  $("#aviso").textContent = "";
  try {
    const params = new URLSearchParams({
      page,
      size: 10,
      busca: $("#busca").value,
    });
    if ($("#status").value) params.set("ativo", $("#status").value);
    const data = await request(api + "?" + params);
    if (seq !== requestNumber) return;
    if (page > 0 && data.content.length === 0) {
      page--;
      return load();
    }
    $("#lista").replaceChildren();
    $("#total").textContent = data.totalElements + " cliente(s) encontrado(s)";
    $("#pagina").textContent =
      `Página ${data.totalPages ? page + 1 : 0} de ${data.totalPages}`;
    $("#anterior").disabled = page === 0;
    $("#proxima").disabled = data.last;
    for (const c of data.content) {
      const tr = document.createElement("tr"),
        td = cell(tr, "");
      const name = document.createElement("strong"),
        email = document.createElement("small");
      name.textContent = c.nome;
      email.textContent = c.email;
      td.append(name, email);
      cell(tr, c.telefone);
      cell(tr, [c.cidade, c.uf].filter(Boolean).join(" / ") || "—");
      const badge = document.createElement("span");
      badge.className = "badge" + (c.ativo ? "" : " off");
      badge.textContent = c.ativo ? "Ativo" : "Inativo";
      cell(tr, "").append(badge);
      const actions = cell(tr, "");
      actions.className = "actions";
      for (const [label, fn, cls] of [
        ["Editar", () => edit(c.id), "secondary"],
        ["Excluir", () => remove(c), "danger"],
      ]) {
        const button = document.createElement("button");
        button.textContent = label;
        button.className = cls;
        button.addEventListener("click", fn);
        actions.append(button);
      }
      $("#lista").append(tr);
    }
    if (!data.content.length) {
      const tr = document.createElement("tr"),
        td = cell(
          tr,
          "Nenhum cliente encontrado. Cadastre um cliente ou ajuste os filtros.",
        );
      td.colSpan = 5;
      td.className = "empty";
      $("#lista").append(tr);
    }
  } catch (e) {
    if (seq === requestNumber) {
      $("#aviso").textContent = e.message;
      $("#total").textContent = "Não foi possível carregar os clientes";
    }
  }
}
function open(c = null) {
  currentId = c?.id ?? null;
  form.reset();
  $("#erro").textContent = "";
  $("#titulo").textContent = c ? "Editar cliente" : "Novo cliente";
  if (c)
    for (const key of [
      "nome",
      "email",
      "telefone",
      "endereco",
      "cidade",
      "uf",
      "cep",
      "ativo",
    ])
      form.elements[key].value = c[key] ?? "";
  modal.showModal();
}
async function edit(id) {
  try {
    open(await request(api + "/" + id));
  } catch (e) {
    $("#aviso").textContent = e.message;
  }
}
async function remove(c) {
  if (!confirm(`Excluir o cliente ${c.nome}? Esta ação não pode ser desfeita.`))
    return;
  try {
    await request(api + "/" + c.id, { method: "DELETE" });
    await load();
  } catch (e) {
    $("#aviso").textContent = e.message;
  }
}
$("#novo").onclick = () => open();
$("#fechar").onclick = $("#cancelar").onclick = () => {
  if (!busy) modal.close();
};
modal.addEventListener("cancel", (e) => {
  if (busy) e.preventDefault();
});
form.addEventListener("submit", async (e) => {
  e.preventDefault();
  if (busy) return;
  const data = Object.fromEntries(new FormData(form));
  for (const key of Object.keys(data)) data[key] = data[key].trim();
  data.ativo = data.ativo === "true";
  for (const key of ["endereco", "cidade", "uf", "cep"])
    if (!data[key]) data[key] = null;
  busy = true;
  $("#salvar").disabled = true;
  $("#erro").textContent = "";
  try {
    await request(api + (currentId ? "/" + currentId : ""), {
      method: currentId ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
    modal.close();
    await load();
  } catch (e) {
    $("#erro").textContent = e.message;
  } finally {
    busy = false;
    $("#salvar").disabled = false;
  }
});
$("#filtros").onsubmit = (e) => {
  e.preventDefault();
  page = 0;
  load();
};
$("#anterior").onclick = () => {
  page--;
  load();
};
$("#proxima").onclick = () => {
  page++;
  load();
};
load();
