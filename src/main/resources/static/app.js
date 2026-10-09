const form = document.querySelector('#contactForm');
const list = document.querySelector('#contacts');
const message = document.querySelector('#message');
const search = document.querySelector('#search');
const save = document.querySelector('#saveButton');
const cancel = document.querySelector('#cancelButton');
let editingId = null;
let loadSequence = 0;
let busy = false;

function showMessage(text, error = false) {
  message.textContent = text;
  message.classList.toggle('error', error);
  message.hidden = false;
}

async function api(path = '', options = {}) {
  const response = await fetch('/api/contacts' + path, options);
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.errors ? Object.values(body.errors).join('. ') : body.message || 'Request failed. Please try again.');
  }
  return response.status === 204 ? null : response.json();
}

// textContent keeps contact data as text, including names containing HTML.
function element(tag, className, text) {
  const node = document.createElement(tag);
  node.className = className;
  node.textContent = text;
  return node;
}

function resetForm() {
  editingId = null;
  form.reset();
  document.querySelector('#formTitle').textContent = 'Add a contact';
  save.textContent = 'Save contact';
  cancel.hidden = true;
}

function editContact(contact) {
  if (busy) return;
  editingId = contact.id;
  for (const field of ['name', 'phone', 'email', 'address']) form.elements[field].value = contact[field];
  document.querySelector('#formTitle').textContent = 'Edit contact';
  save.textContent = 'Update contact';
  cancel.hidden = false;
  form.elements.name.focus();
  form.scrollIntoView({behavior: 'smooth', block: 'center'});
}

function setBusy(value) {
  busy = value;
  for (const control of form.elements) control.disabled = value;
  list.querySelectorAll('button').forEach(button => button.disabled = value);
}

async function deleteContact(contact) {
  if (busy || !confirm(`Delete ${contact.name}? This cannot be undone.`)) return;
  setBusy(true);
  try {
    await api('/' + contact.id, {method: 'DELETE'});
    if (editingId === contact.id) resetForm();
    showMessage('Contact deleted.');
    await loadContacts();
  } catch (error) { showMessage(error.message, true); }
  finally { setBusy(false); }
}

async function loadContacts() {
  const sequence = ++loadSequence;
  try {
    const contacts = await api('?search=' + encodeURIComponent(search.value.trim()));
    if (sequence !== loadSequence) return;
    list.replaceChildren();
    document.querySelector('#count').textContent = contacts.length;
    if (!contacts.length) list.append(element('p', 'empty', search.value.trim() ? 'No contacts match your search.' : 'Your contact book is ready. Add your first contact using the form.'));
    for (const contact of contacts) {
      const card = element('article', 'contact', '');
      card.append(element('div', 'avatar', contact.name.slice(0, 1).toUpperCase()));
      const content = element('div', '', '');
      content.append(element('h3', '', contact.name));
      content.append(element('p', 'detail', contact.phone));
      if (contact.email) content.append(element('p', 'detail', contact.email));
      if (contact.address) content.append(element('p', 'detail', contact.address));
      const actions = element('div', 'actions', '');
      const edit = element('button', 'edit', 'Edit');
      edit.type = 'button';
      edit.setAttribute('aria-label', 'Edit ' + contact.name);
      edit.disabled = busy;
      edit.addEventListener('click', () => editContact(contact));
      const remove = element('button', 'delete', 'Delete');
      remove.type = 'button';
      remove.setAttribute('aria-label', 'Delete ' + contact.name);
      remove.disabled = busy;
      remove.addEventListener('click', () => deleteContact(contact));
      actions.append(edit, remove);
      content.append(actions);
      card.append(content);
      list.append(card);
    }
  } catch (error) {
    if (sequence !== loadSequence) return;
    list.replaceChildren(element('p', 'empty', 'Could not load contacts. Search again to retry.'));
    document.querySelector('#count').textContent = '—';
    showMessage(error.message, true);
  }
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  if (busy) return;
  const data = Object.fromEntries(new FormData(form));
  const updating = editingId !== null;
  setBusy(true);
  try {
    await api(updating ? '/' + editingId : '', {
      method: updating ? 'PUT' : 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(data)
    });
    resetForm();
    search.value = '';
    showMessage(updating ? 'Contact updated.' : 'Contact saved.');
    await loadContacts();
  } catch (error) { showMessage(error.message, true); }
  finally { setBusy(false); }
});
cancel.addEventListener('click', resetForm);
let searchTimer;
search.addEventListener('input', () => {
  ++loadSequence;
  clearTimeout(searchTimer);
  searchTimer = setTimeout(loadContacts, 250);
});
loadContacts();
