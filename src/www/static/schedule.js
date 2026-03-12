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
 function onHourlyCheckboxChanged(hourlyCheckbox) {
  if (hourlyCheckbox.checked) {
    const numRingsElement = document.getElementById("numrings");
    numRingsElement.value = "";
  }
}

function onNumRingsChanged(numRings) {
  if (numRings.value > 999) {
    numRings.value = Math.floor(numRings.value / 10)
  }
  if (numRings.value != "") {
    const hourlyCheckbox = document.getElementById("ishourly");
    hourlyCheckbox.checked = false;
  }
}

function onPeriodChanged(period) {
  if (period.value > 9999) {
    period.value = Math.floor(period.value / 10);
  } else if (period.value > 1440) {
    period.value = 1440
  }
}

function onMinuteChanged(input) {
  if (input.value > 99) {
    input.value = Math.floor(input.value / 10);
  } else if (input.value > 59) {
    input.value = 59
  }
}

function onHourChanged(input) {
  if (input.value > 99) {
    input.value = Math.floor(input.value / 10);
  } else if (input.value > 12) {
    input.value = 12
  }
}

function onSilenceChanged(silence) {
  if (silence.value > 999) {
    silence.value = Math.floor(silence.value / 10);
  }
}

function onAdd() {
  onAddOrUpdate('/api/add');
}

function onUpdate() {
  onAddOrUpdate('/api/update');
}

function onAddOrUpdate(url) {
  const form = document.querySelector('form');
  const data = Object.fromEntries(new FormData(form));
  sendRequest('POST', url, data, function(err, response) {
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
