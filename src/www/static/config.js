/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
function onRingDurationChanged(period) {
  if (period.value > 99999) {
    period.value = Math.floor(period.value / 10);
  } else if (period.value > 30000) {
    period.value = 30000;
  }
}

function onDefaultSilenceDurationChanged(input) {
  if (input.value > 99999) {
    input.value = Math.floor(input.value / 10);
  } else if (input.value > 60000) {
    input.value = 60000;
  }
}

function onSave(url) {
  const form = document.querySelector('form');
  const data = Object.fromEntries(new FormData(form));
  sendRequest('POST', '/api/config', data, function(err, response) {
    if (err) {
      restoreBorders();
      document.getElementById("errorMessage").innerText = "";

      if (err.errorFields) {
        highlightElements(err.errorFields);
      }
      if (err.errorMessage) {
        document.getElementById("errorMessage").innerText = err.errorMessage;
      }
    } else {
      location.href = "/";
    }
  });
}

savedBorders = {}

function getElement(id) {
  const el = document.getElementById(id);
  if (el.type === 'checkbox') return el.closest('div');
  return el;
}

function highlightElements(ids) {
  ids.forEach(id => {
    const el = getElement(id);
    savedBorders[id] = el.style.border;
    el.style.border = "3px solid red";
  });
}

function restoreBorders() {
  Object.entries(savedBorders).forEach(([id, border]) => {
    const el = getElement(id);
    el.style.border = border;
  });
}

function onCancel() {
  location.href = "/";
}
